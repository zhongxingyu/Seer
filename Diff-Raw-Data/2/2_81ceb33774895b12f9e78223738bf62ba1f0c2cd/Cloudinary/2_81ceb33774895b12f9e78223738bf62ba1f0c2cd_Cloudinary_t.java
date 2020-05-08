 /*
  * Copyright © 2012 Avego Ltd., All Rights Reserved.
  * For licensing terms please contact Avego LTD.
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.avego.cloudinary;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.log4j.Logger;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 /**
  * Functionality for using the Cloudinary CDN. See: https://cloudinary.com/documentation/upload_images
  * @version $Id$
  * @author eoghancregan
  */
 public class Cloudinary {
 
 	private static final Logger LOGGER = Logger.getLogger(Cloudinary.class);
 	private CloudinaryRepository cloudinaryRepository;
 
 	/**
 	 * This creates a Cloudinary
 	 * @param cloudinaryRepository
 	 */
 	public Cloudinary(CloudinaryRepository cloudinaryRepository) {
 
 		this.cloudinaryRepository = cloudinaryRepository;
 	}
 
 	/**
 	 * Encodes the provided bytes in Base 64, parcels it up in a signed JSON Object and fires it at Cloudinary.
 	 * @param bytes
 	 * @param mimeType
 	 * @return
 	 * @throws CloudinaryException
 	 */
 	public String postPhotoToCloudinary(byte[] bytes, String mimeType) throws CloudinaryException {
 
 		PostMethod post = new PostMethod(this.cloudinaryRepository.getUploadUrl());
 
 		long currentTime = new Date().getTime();
 
 		String signature = generateSignature(currentTime, null);
 
 		String base64RepresentationOfImage = Base64.encodeBytes(bytes);
 		StringBuilder payload = buildCreationPayload(base64RepresentationOfImage, currentTime, signature, mimeType);
 
 		LOGGER.trace("Sending JSON payload to Cloudinary: " + payload);
 
 		String response = postJsonToCloudinary(post, payload);
 
 		return extractPublicIdFromJson(response);
 	}
 
 	private String extractPublicIdFromJson(String response) throws CloudinaryException {
 		String publicId = null;
 
 		try {
 			JSONObject jsonObject = new JSONObject(response);
 			publicId = (String) jsonObject.get("public_id");
 		} catch (JSONException e) {
 			throw new CloudinaryException("Failed to Parse Cloudinary Response as JSON: " + response);
 		}
 		return publicId;
 	}
 
 	private String postJsonToCloudinary(PostMethod post, StringBuilder payload) throws CloudinaryException {
 
 		HttpClient client = new HttpClient();
 
 		StringRequestEntity body;
 		try {
 			body = new StringRequestEntity(payload.toString(), "application/json", "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new CloudinaryException("JSON is not supported by Implementation of String Request Entity.", e);
 		}
 		post.setRequestEntity(body);
 
 		int status;
 		try {
 			status = client.executeMethod(post);
 		} catch (IOException e) {
 			throw new CloudinaryException("A HTTP Exception Occurred while attempting to POST JSON to CLoudinary.", e);
 		}
 
 		String response = null;
 		try {
 			response = post.getResponseBodyAsString();
 		} catch (IOException e) {
 			throw new CloudinaryException("Failed to Retrieve response from POST Method.", e);
 		}
 
		if (status != 200) {
 
 			throw new CloudinaryException("Cloudinary Operation was Unsuccessful.  Response was: " + response);
 		}
 
 		LOGGER.info("POST was successfully sent to Cloudinary, response was: " + response);
 		return response;
 	}
 
 	private StringBuilder buildCreationPayload(String base64RepresentationOfImage, long currentTime, String signature, String mimeType) {
 		StringBuilder payload = new StringBuilder();
 		payload.append("{ \"file\":");
 		payload.append("\"data:" + mimeType + ";base64," + base64RepresentationOfImage + "\"");
 		payload.append(",\"api_key\":");
 		payload.append("\"");
 		payload.append(this.cloudinaryRepository.getApiKey());
 		payload.append("\"");
 		payload.append(",\"timestamp\":");
 		payload.append("\"");
 		payload.append(Long.toString(currentTime));
 		payload.append("\"");
 		payload.append(",\"signature\":");
 		payload.append("\"");
 		payload.append(signature);
 		payload.append("\"");
 		payload.append("}");
 		return payload;
 	}
 
 	private StringBuilder buildDeletionPayload(String publicId, long currentTime, String signature) {
 		StringBuilder payload = new StringBuilder();
 		payload.append("{ \"public_id\":");
 		payload.append("\"");
 		payload.append(publicId);
 		payload.append("\"");
 		payload.append(",\"api_key\":");
 		payload.append("\"");
 		payload.append(this.cloudinaryRepository.getApiKey());
 		payload.append("\"");
 		payload.append(",\"timestamp\":");
 		payload.append("\"");
 		payload.append(Long.toString(currentTime));
 		payload.append("\"");
 		payload.append(",\"signature\":");
 		payload.append("\"");
 		payload.append(signature);
 		payload.append("\"");
 		payload.append("}");
 		return payload;
 	}
 
 	private String generateSignature(long currentTime, String publicId) throws CloudinaryException {
 		MessageDigest messageDigest;
 		try {
 			messageDigest = MessageDigest.getInstance("SHA1");
 		} catch (NoSuchAlgorithmException e) {
 			throw new CloudinaryException("No SHA1 Message Digest Algorithm was available.", e);
 		}
 
 		StringBuilder stringToSign = new StringBuilder();
 
 		if (publicId != null) {
 
 			stringToSign.append("public_id=");
 			stringToSign.append(publicId);
 			stringToSign.append("&");
 		}
 
 		stringToSign.append("timestamp=");
 		stringToSign.append(Long.toString(currentTime));
 		stringToSign.append(this.cloudinaryRepository.getSecretkey());
 
 		messageDigest.update(stringToSign.toString().getBytes());
 		String unpaddedSha1Hex = new BigInteger(1, messageDigest.digest()).toString(16);
 		String sha1Hex = String.format("%40s", unpaddedSha1Hex).replace(' ', '0');
 		return sha1Hex;
 	}
 
 	/**
 	 * Builds a URL pointing the Image with the provided Public Id, Mime Type and Meta Info.
 	 * @param publicId
 	 * @param mimeType
 	 * @param width
 	 * @param height
 	 * @return
 	 * @throws CloudinaryException
 	 */
 	public URI buildCloudinaryPhotoURI(String publicId, String mimeType, int width, int height) throws CloudinaryException {
 
 		StringBuilder urlBuilder = new StringBuilder(this.cloudinaryRepository.getDownloadUrl());
 
 		applyTransformation(width, height, urlBuilder);
 
 		urlBuilder.append("/");
 
 		urlBuilder.append(publicId);
 
 		if (mimeType == null || mimeType == "image/jpeg") {
 
 			urlBuilder.append(".jpg");
 		}
 
 		try {
 			return new URI(urlBuilder.toString());
 		} catch (URISyntaxException e) {
 			throw new CloudinaryException("Failed to Generate valid URI from public id: " + publicId, e);
 		}
 	}
 
 	private void applyTransformation(int width, int height, StringBuilder urlBuilder) {
 
 		if (width > 0 || height > 0) {
 
 			urlBuilder.append("/");
 
 			if (width > 0) {
 
 				urlBuilder.append("w_");
 				urlBuilder.append(Integer.toString(width));
 
 				urlBuilder.append(",");
 			}
 
 			if (height > 0) {
 
 				urlBuilder.append("h_");
 				urlBuilder.append(Integer.toString(height));
 
 				urlBuilder.append(",");
 			}
 
 			urlBuilder.append("c_fill");
 		}
 	}
 
 	/**
 	 * Deletes an Image with the associated Public Id if one exists.
 	 * @param publicId
 	 * @throws CloudinaryException
 	 */
 	public void deleteImageFromCloudinary(String publicId) throws CloudinaryException {
 
 		PostMethod post = new PostMethod(this.cloudinaryRepository.getDeletionUrl());
 
 		long currentTime = new Date().getTime();
 
 		String signature = generateSignature(currentTime, publicId);
 
 		StringBuilder payload = buildDeletionPayload(publicId, currentTime, signature);
 
 		LOGGER.trace("Sending JSON payload to Cloudinary: " + payload);
 
 		postJsonToCloudinary(post, payload);
 	}
 }
