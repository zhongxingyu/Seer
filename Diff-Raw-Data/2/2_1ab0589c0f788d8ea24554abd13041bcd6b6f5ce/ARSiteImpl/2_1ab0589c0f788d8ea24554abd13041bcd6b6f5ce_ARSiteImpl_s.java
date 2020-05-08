 /*
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.parworks.androidlibrary.ar;
 
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.entity.mime.content.StringBody;
 
 import android.os.AsyncTask;
 
 import com.parworks.androidlibrary.response.ARResponseHandler;
 import com.parworks.androidlibrary.response.ARResponseHandlerImpl;
 import com.parworks.androidlibrary.response.AddBaseImageResponse;
 import com.parworks.androidlibrary.response.AddSaveOverlayResponse;
 import com.parworks.androidlibrary.response.AugmentImageResponse;
 import com.parworks.androidlibrary.response.AugmentImageResultResponse;
 import com.parworks.androidlibrary.response.BaseImageInfo;
 import com.parworks.androidlibrary.response.BasicResponse;
 import com.parworks.androidlibrary.response.GetSiteInfoResponse;
 import com.parworks.androidlibrary.response.GetSiteOverlaysResponse;
 import com.parworks.androidlibrary.response.ImageOverlayInfo;
 import com.parworks.androidlibrary.response.InitiateBaseImageProcessingResponse;
 import com.parworks.androidlibrary.response.ListBaseImagesResponse;
 import com.parworks.androidlibrary.response.OverlayAugmentResponse;
 import com.parworks.androidlibrary.response.SiteInfo;
 import com.parworks.androidlibrary.response.SiteInfo.BaseImageState;
 import com.parworks.androidlibrary.response.SiteInfo.OverlayState;
 import com.parworks.androidlibrary.response.SiteInfoSummary;
 import com.parworks.androidlibrary.utils.HttpUtils;
 
 public class ARSiteImpl implements ARSite {
 
 	private final String mId;
 	private final String mApiKey;
 	private final String mSignature;
 	private final String mTime;
 
 	private static final int REQUIRED_NUMBER_OF_BASE_IMAGES = 1;
 
 	public ARSiteImpl(String siteId, String apiKey, String time,
 			String signature) {
 		mId = siteId;
 		mApiKey = apiKey;
 		mSignature = signature;
 		mTime = time;
 	}
 
 	@Override
 	public void getBaseImages(final ARListener<List<BaseImageInfo>> listener) {
 		new AsyncTask<Void, Void, List<BaseImageInfo>>() {
 
 			@Override
 			protected List<BaseImageInfo> doInBackground(Void... arg0) {
 				return getBaseImages();
 			}		
 			@Override
 			protected void onPostExecute(List<BaseImageInfo> result) {
 				listener.handleResponse(result);
 			}
 			
 			
 		}.execute();
 	
 
 	}
 
 	@Override
 	public void getSiteInfo(final ARListener<SiteInfo> listener) {
 		new AsyncTask<Void, Void, SiteInfo>() {
 			@Override
 			protected SiteInfo doInBackground(Void... params) {
 				return getSiteInfo();
 			}
 			@Override
 			protected void onPostExecute(SiteInfo result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 
 	}
 
 
 
 
 	@Override
 	public void addBaseImage(final String filename, final InputStream image,
 			final ARListener<BaseImage> listener) {
 		
 		new AsyncTask<Void, Void, BaseImage>() {
 
 			@Override
 			protected BaseImage doInBackground(Void... params) {
 				return addBaseImage(filename, image);
 			}
 			
 			@Override
 			protected void onPostExecute(BaseImage result) {
 				listener.handleResponse(result);
 			}
 			
 		}.execute();
 
 
 	}
 
 	@Override
 	public void processBaseImages(final BaseImageProcessingProfile profile, final ARListener<State> listener) {
 		
 		new AsyncTask<Void, Void, State>() {
 			@Override
 			protected State doInBackground(Void... params) {
 				return processBaseImages(profile);
 			}
 			@Override
 			protected void onPostExecute(State result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 
 	}
 
 	@Override
 	public void getState(final ARListener<State> listener) {
 		new AsyncTask<Void, Void, State>() {
 			@Override
 			protected State doInBackground(Void... params) {
 				return getState();
 			}
 			@Override
 			protected void onPostExecute(State result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 
 	}
 
 	@Override
 	public void addOverlay(final Overlay overlay,
 			final ARListener<OverlayResponse> listener) {
 		
 		new AsyncTask<Void, Void, OverlayResponse>() {
 			@Override
 			protected OverlayResponse doInBackground(Void... params) {
 				return addOverlay(overlay);
 			}
 			@Override
 			protected void onPostExecute(OverlayResponse result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 
 	}
 
 	@Override
 	public void updateOverlay(final OverlayResponse overlayToUpdate, final Overlay newOverlay,
 			final ARListener<OverlayResponse> listener) {
 		new AsyncTask<Void, Void, OverlayResponse>() {
 			@Override
 			protected OverlayResponse doInBackground(Void... params) {
 				return updateOverlay(overlayToUpdate, newOverlay);
 			}
 			@Override
 			protected void onPostExecute(OverlayResponse result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 	}
 
 	@Override
 	public void deleteOverlay(final OverlayResponse overlay, final ARListener<Boolean> listener) {
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				return deleteOverlay(overlay);
 			}
 			@Override
 			protected void onPostExecute(Boolean result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 
 
 	}
 
 	@Override
 	public void augmentImage(final InputStream image,
 			final ARListener<AugmentedData> listener) {
 		new AsyncTask<Void, Void, AugmentedData>() {
 
 			@Override
 			protected AugmentedData doInBackground(Void... arg0) {
 				return augmentImage(image);
 			}
 
 			@Override
 			protected void onPostExecute(AugmentedData result) {
 				listener.handleResponse(result);
 
 			};
 
 		}.execute();
 
 	}
 
 
 	@Override
 	public void delete(final ARListener<Boolean> listener) {
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				return delete();
 			}
 			@Override
 			protected void onPostExecute(Boolean result) {
 				// TODO Auto-generated method stub
 				super.onPostExecute(result);
 			}
 		}.execute();
 
 
 	}
 
 	/*
 	 * 
 	 * 
 	 * Sync
 	 */
 
 	@Override
 	public BaseImage addBaseImage(String filename, InputStream image) {
 		handleStateSync(mId, State.NEEDS_MORE_BASE_IMAGES,
 				State.NEEDS_BASE_IMAGE_PROCESSING);
 		// make httputils
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 
 		// make query string
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 		params.put("filename", filename);
 
 		// make entity
 		MultipartEntity imageEntity = new MultipartEntity();
 		InputStreamBody imageInputStreamBody = new InputStreamBody(image,
 				filename);
 		imageEntity.addPart("image", imageInputStreamBody);
 
 		// do post
 		HttpResponse serverResponse = httpUtils
 				.doPost(HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.ADD_BASE_IMAGE_PATH, imageEntity, params);
 
 		// handle status code
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		// parse response
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		AddBaseImageResponse addBaseImageResponse = responseHandler
 				.handleResponse(serverResponse, AddBaseImageResponse.class);
 
 		// return baseimageinfo
 		if (addBaseImageResponse.getSuccess() == true) {
 			return new BaseImage(addBaseImageResponse.getId());
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server but failed to add the base image. Perhaps the site does not exist, or there is a problem with the image.");
 		}
 
 	}
 
 	@Override
 	public State processBaseImages(BaseImageProcessingProfile profile) {
 		handleStateSync(mId, State.NEEDS_BASE_IMAGE_PROCESSING);
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 		
 		String profileString = profile.name().replace("_", "-").toLowerCase();
 		params.put("profile", profileString);
 
 		HttpResponse serverResponse = httpUtils
 				.doGet(HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.INITIATE_BASE_IMAGE_PROCESSING_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		InitiateBaseImageProcessingResponse initProcessingResponse = responseHandler
 				.handleResponse(serverResponse,
 						InitiateBaseImageProcessingResponse.class);
 
 		if (initProcessingResponse.getSuccess() == true) {
 			return State.NEEDS_OVERLAYS;
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server but failed to process the base images. Perhaps the site was deleted.");
 		}
 	}
 
 	@Override
 	public State getState() {
 
 		SiteInfo siteInfo = getSiteInfo();
 		return determineSiteState(siteInfo.getBimState(),
 				siteInfo.getSiteState(), siteInfo.getTotalImages());
 
 	}
 
 	public static State determineSiteState(OverlayState bimState,
 			BaseImageState siteState, int baseImageTotal) {
 		OverlayState overlayState = bimState;
 		BaseImageState baseImageState = siteState;
 		if ((overlayState == OverlayState.NOT_PROCESSED)
 				&& (baseImageState == BaseImageState.NOT_PROCESSED)) {
 			if (baseImageTotal >= REQUIRED_NUMBER_OF_BASE_IMAGES) {
 				return State.NEEDS_BASE_IMAGE_PROCESSING;
 			} else {
 				return State.NEEDS_MORE_BASE_IMAGES;
 			}
 		} else if ((baseImageState == BaseImageState.PROCESSED)
 				&& (overlayState == OverlayState.NOT_PROCESSED)) {
 			return State.NEEDS_OVERLAYS;
 		} else if ((baseImageState == BaseImageState.PROCESSED)
 				&& (overlayState == OverlayState.PROCESSED)) {
 			return State.READY_TO_AUGMENT_IMAGES;
 		} else if ((baseImageState == BaseImageState.PROCESSING)
 				|| (overlayState == OverlayState.PROCESSING)) {
 			return State.PROCESSING;
 		} else if ((baseImageState == BaseImageState.PROCESSING_FAILED)) {
 			if (baseImageTotal >= REQUIRED_NUMBER_OF_BASE_IMAGES) {
 				return State.NEEDS_BASE_IMAGE_PROCESSING;
 			} else {
 				return State.NEEDS_MORE_BASE_IMAGES;
 			}
 		} else if (overlayState == OverlayState.PROCESSING_FAILED) {
 			return State.NEEDS_OVERLAYS;
 		} else {
 			throw new ARException(
 					"An error occured. The site is in an undefined state.");
 		}
 	}
 
 	@Override
 	public OverlayResponse addOverlay(Overlay overlay) {
 		handleStateSync(mId, State.NEEDS_OVERLAYS,
 				State.READY_TO_AUGMENT_IMAGES);
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 		params.put("imgId", overlay.getImageId());
 		params.put("name", overlay.getName());
 		params.put("content", overlay.getDescription());
 
 		List<Vertex> vertices = overlay.getVertices();
 		MultipartEntity entity = new MultipartEntity();
 
 		for (Vertex currentVertex : vertices) {
 			try {
 				entity.addPart("v", new StringBody((int) currentVertex.getxCoord()
 						+ "," + (int) currentVertex.getyCoord()));
 			} catch (UnsupportedEncodingException e) {
 				throw new ARException(e);
 			}
 		}
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doPost(
 				HttpUtils.PARWORKS_API_BASE_URL + HttpUtils.ADD_OVERLAY_PATH,
 				entity, params);
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		AddSaveOverlayResponse addOverlayResponse = responseHandler
 				.handleResponse(serverResponse, AddSaveOverlayResponse.class);
 
 		if (addOverlayResponse.getSuccess() == true) {
 			return new OverlayResponse(addOverlayResponse.getId());
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but failed to add the overlay. Perhaps the site no longer exists, or there was a problem with the overlay.");
 		}
 	}
 
 	@Override
 	public OverlayResponse updateOverlay(OverlayResponse overlayToUpdate,
 			Overlay newOverlay) {
 		handleStateSync(mId, State.READY_TO_AUGMENT_IMAGES);
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 		params.put("id", overlayToUpdate.getOverlayId());
 		params.put("imgId", newOverlay.getImageId());
 		params.put("name", newOverlay.getName());
 		params.put("content", newOverlay.getDescription());
 
 		List<Vertex> vertices = newOverlay.getVertices();
 		MultipartEntity entity = new MultipartEntity();
 
 		for (Vertex currentVertex : vertices) {
 			try {
 				entity.addPart("v", new StringBody(currentVertex.getxCoord()
 						+ "," + currentVertex.getyCoord()));
 			} catch (UnsupportedEncodingException e) {
 				throw new ARException(e);
 			}
 		}
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doPost(
 				HttpUtils.PARWORKS_API_BASE_URL + HttpUtils.SAVE_OVERLAY_PATH,
 				entity, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		AddSaveOverlayResponse saveOverlayResponse = responseHandler
 				.handleResponse(serverResponse, AddSaveOverlayResponse.class);
 
 		if (saveOverlayResponse.getSuccess() == true) {
 			return new OverlayResponse(saveOverlayResponse.getId());
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but failed to update the overlay. Perhaps the site no longer exists, or there was a problem with the overlay.");
 		}
 	}
 
 	@Override
 	public Boolean deleteOverlay(OverlayResponse overlay) {
		handleStateSync(mId, State.READY_TO_AUGMENT_IMAGES);
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 		params.put("id", overlay.getOverlayId());
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils
 				.doPost(HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.REMOVE_OVERLAY_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		BasicResponse deleteOverlayResponse = responseHandler.handleResponse(
 				serverResponse, BasicResponse.class);
 
 		if (deleteOverlayResponse.getSuccess() == false) {
 			throw new ARException(
 					"Successfully communicated with the server, but the overlay was not deleted. Perhaps it does not exist.");
 		} else {
 			return true;
 		}
 
 	}
 
 	public String startImageAugment(InputStream image) {
 		handleStateSync(mId, State.READY_TO_AUGMENT_IMAGES);
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		MultipartEntity imageEntity = new MultipartEntity();
 		InputStreamBody imageInputStreamBody = new InputStreamBody(image,
 				"image");
 		imageEntity.addPart("image", imageInputStreamBody);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doPost(
 				HttpUtils.PARWORKS_API_BASE_URL + HttpUtils.AUGMENT_IMAGE_PATH,
 				imageEntity, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		AugmentImageResponse augmentImageResponse = responseHandler
 				.handleResponse(serverResponse, AugmentImageResponse.class);
 		
 		if (augmentImageResponse.getSuccess() == false) {
 			throw new ARException(
 					"Successfully communicated with the server, failed to augment the image. Perhaps the site does not exist or has no overlays.");
 		}
 
 		return augmentImageResponse.getImgId();
 
 	}
 
 	public AugmentedData getAugmentResult(String imgId) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("imgId", imgId);
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.AUGMENT_IMAGE_RESULT_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		if (serverResponse.getStatusLine().getStatusCode() == 204) {
 			return null;
 		}
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		AugmentImageResultResponse result = responseHandler.handleResponse(
 				serverResponse, AugmentImageResultResponse.class);
 
 		return convertAugmentResultResponse(imgId, result);
 	}
 
 	@Override
 	public AugmentedData augmentImage(InputStream image) {
 		handleStateSync(mId, State.READY_TO_AUGMENT_IMAGES);
 		String imageId = startImageAugment(image);
 
 		AugmentedData augmentedImage = null;
 		while (augmentedImage == null) {
 			augmentedImage = getAugmentResult(imageId);
 		}
 		return augmentedImage;
 	}
 
 	@Override
 	public Boolean delete() {
 
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL + HttpUtils.REMOVE_SITE_PATH,
 				params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		BasicResponse deleteSiteResponse = responseHandler.handleResponse(
 				serverResponse, BasicResponse.class);
 
 		if (deleteSiteResponse.getSuccess() == false) {
 			throw new ARException(
 					"Successfully communicated with the server, but was unable to delete the site. Perhaps the site no longer exists.");
 		} else {
 			return true;
 		}
 
 	}
 
 	@Override
 	public SiteInfo getSiteInfo() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL + HttpUtils.GET_SITE_INFO_PATH,
 				params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		GetSiteInfoResponse getSiteInfoResponse = responseHandler
 				.handleResponse(serverResponse, GetSiteInfoResponse.class);
 
 		if (getSiteInfoResponse.getSuccess() == true) {
 			SiteInfo siteInfo = getSiteInfoResponse.getSite();
 			siteInfo.setTotalImages(getSiteInfoResponse.getTotalImages());
 			return siteInfo;
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but was unable to get site info. Perhaps the site no longer exists. The id was: "
 							+ mId);
 		}
 
 	}
 
 	@Override
 	public List<BaseImageInfo> getBaseImages() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.LIST_BASE_IMAGES_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		ListBaseImagesResponse baseImagesResponse = responseHandler
 				.handleResponse(serverResponse, ListBaseImagesResponse.class);
 
 		if (baseImagesResponse.getSuccess() == true) {
 			return baseImagesResponse.getImages();
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but was unable to get base images. Perhaps the site no longer exists.");
 		}
 	}
 
 	/**
 	 * Makes a call to the synchronous getState() method, then throws an
 	 * ARException if the state is not the required state
 	 * 
 	 * @param siteId
 	 *            the id of the site
 	 * @param requiredState
 	 */
 	private void handleStateSync(String siteId, State requiredState) {
 		handleStateSync(siteId, requiredState, null);
 	}
 
 	/**
 	 * Makes a call to the synchronous getState() method, then throws an
 	 * ARException if the state is not the required state
 	 * 
 	 * @param siteId
 	 * @param firstPossibleState
 	 * @param secondPossibleState
 	 */
 	private void handleStateSync(String siteId, final State firstPossibleState,
 			final State secondPossibleState) {
 		State siteState = getState();
 		if ((siteState == firstPossibleState)
 				|| (siteState == secondPossibleState)) {
 			return;
 		} else {
 			throw new ARException("State was " + siteState + ". State must be "
 					+ firstPossibleState + " or " + secondPossibleState);
 		}
 	}
 
 	@Override
 	public String getSiteId() {
 		return mId;
 	}
 
 	private AugmentedData convertAugmentResultResponse(String imgId,
 			AugmentImageResultResponse result) {
 		List<OverlayAugmentResponse> overlayResponses = result.getOverlays();
 		List<Overlay> overlays = new ArrayList<Overlay>();
 
 		for (OverlayAugmentResponse overlayResponse : overlayResponses) {
 			overlays.add(makeOverlay(overlayResponse, imgId));
 		}
 
 		AugmentedData augmentedData = new AugmentedData(result.getFov(),
 				result.getFocalLength(), result.getScore(),
 				result.isLocalization(), overlays);
 		return augmentedData;
 	}
 
 	private Overlay makeOverlay(OverlayAugmentResponse overlayResponse,
 			String imgId) {
 		Overlay overlay = new OverlayImpl(imgId, overlayResponse.getName(),
 				overlayResponse.getDescription(),
 				parseVertices(overlayResponse.getVertices()));
 		return overlay;
 
 	}
 
 	private List<Vertex> parseVertices(String serverOutput) {
 		String[] points = serverOutput.split(",");
 
 		List<Vertex> vertices = new ArrayList<Vertex>();
 		for (int i = 0; i < points.length; i += 3) {
 			float xCoord = Float.parseFloat(points[i]);
 			float yCoord = Float.parseFloat(points[i + 1]);
 			float zCoord = Float.parseFloat(points[i + 2]);
 			vertices.add(new Vertex(xCoord, yCoord, zCoord));
 		}
 		return vertices;
 	}
 
 	@Override
 	public SiteInfoSummary getSiteInfoSummary() {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.GET_SITE_INFO_SUMMARY_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		SiteInfoSummary siteInfoSummary = responseHandler
 				.handleResponse(serverResponse, SiteInfoSummary.class);
 
 		if (siteInfoSummary != null) {
 			return siteInfoSummary;
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but was unable to get site info summary. Perhaps the site no longer exists.");
 		}	
 	}
 	
 	@Override
 	public void getSiteInfoSummary(final ARListener<SiteInfoSummary> listener) {
 		new AsyncTask<Void, Void, SiteInfoSummary>() {
 			@Override
 			protected SiteInfoSummary doInBackground(Void... params) {
 				return getSiteInfoSummary();
 			}
 			@Override
 			protected void onPostExecute(SiteInfoSummary result) {
 				listener.handleResponse(result);
 			}
 		}.execute();
 	}
 
 	@Override
 	public List<ImageOverlayInfo> getSiteOverlays(String siteId) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("site", mId);
 
 		HttpUtils httpUtils = new HttpUtils(mApiKey, mTime, mSignature);
 		HttpResponse serverResponse = httpUtils.doGet(
 				HttpUtils.PARWORKS_API_BASE_URL
 						+ HttpUtils.GET_SITE_OVERLAYS_PATH, params);
 
 		HttpUtils.handleStatusCode(serverResponse.getStatusLine()
 				.getStatusCode());
 
 		ARResponseHandler responseHandler = new ARResponseHandlerImpl();
 		GetSiteOverlaysResponse getSiteOverlaysResponse = responseHandler
 				.handleResponse(serverResponse, GetSiteOverlaysResponse.class);
 
 		if (getSiteOverlaysResponse.getSuccess()) {
 			return getSiteOverlaysResponse.getOverlays();
 		} else {
 			throw new ARException(
 					"Successfully communicated with the server, but was unable to get site info summary. Perhaps the site no longer exists.");
 		}
 	}
 
 	@Override
 	public void getSiteOverlays(final String siteId,
 			final ARListener<List<ImageOverlayInfo>> listener) {
 		new AsyncTask<Void, Void, List<ImageOverlayInfo>>() {
 			@Override
 			protected List<ImageOverlayInfo> doInBackground(Void... params) {
 				return getSiteOverlays(siteId);
 			}
 			@Override
 			protected void onPostExecute(List<ImageOverlayInfo> result) {
 				listener.handleResponse(result);
 			}
 		}.execute();		
 	}
 
 }
