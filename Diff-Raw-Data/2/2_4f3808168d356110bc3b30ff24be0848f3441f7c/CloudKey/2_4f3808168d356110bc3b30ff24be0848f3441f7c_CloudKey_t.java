 package net.dmcloud.cloudkey;
 
 import java.io.*;
 import java.util.Map;
 
 import net.dmcloud.util.*;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class CloudKey extends Api
 {
 	public CloudKey(String _user_id, String _api_key)
 	{
 		super(_user_id, _api_key, CloudKey.API_URL, CloudKey.CDN_URL, "");
 	}
 
 	public CloudKey(String _user_id, String _api_key, String _base_url, String _cdn_url, String _proxy)
 	{
 		super(_user_id, _api_key, _base_url, _cdn_url, _proxy);
 	}
 
 	public String mediaGetEmbedUrl(String id) throws DCException
 	{
 		return this.mediaGetEmbedUrl(CloudKey.API_URL, id, CloudKey.SECLEVEL_NONE, "", "", "", null, null, 0);
 	}
 
 	public String mediaGetEmbedUrl(String id, int seclevel, String asnum, String ip, String useragent, String[] countries, String[] referers, int expires) throws DCException
 	{
 		return this.mediaGetEmbedUrl(CloudKey.API_URL, id, seclevel, asnum, ip, useragent, countries, referers, expires);
 	}
 
 	public String mediaGetEmbedUrl(String url, String id, int seclevel, String asnum, String ip, String useragent, String[] countries, String[] referers, int expires) throws DCException
 	{
 		String _url = url + "/embed/" + this.user_id + "/" + id;
 		return Helpers.sign_url(_url, this.api_key, seclevel, asnum, ip, useragent, countries, referers, expires);
 	}
 
 	public String mediaGetStreamUrl(String id) throws DCException
 	{
 		return this.mediaGetStreamUrl(CloudKey.API_URL, id, "mp4_h264_aac", CloudKey.SECLEVEL_NONE, "", "", "", null, null, 0, "", false);
 	}
 
 	public String mediaGetStreamUrl(String id, String asset_name) throws DCException
 	{
 		return this.mediaGetStreamUrl(CloudKey.API_URL, id, asset_name, CloudKey.SECLEVEL_NONE, "", "", "", null, null, 0, "", false);
 	}
 
 	public String mediaGetStreamUrl(String id, String asset_name, int seclevel, String asnum, String ip, String useragent, String[] countries, String[] referers, int expires, String extension, Boolean download) throws DCException
 	{
 		return this.mediaGetStreamUrl(CloudKey.API_URL, id, asset_name, seclevel, asnum, ip, useragent, countries, referers, expires, extension, download);
 	}
 
 	public String mediaGetStreamUrl(String url, String id, String asset_name, int seclevel, String asnum, String ip, String useragent, String[] countries, String[] referers, int expires, String extension, Boolean download)  throws DCException
 	{
 		if (extension.equals(""))
 		{
 			String[] parts = asset_name.split("\\_");
 			extension = (!parts[0].equals(asset_name)) ? parts[0] : extension;
 		}
		if (asset_name.length() >= 15 && asset_name.substring(0, 15).equals("jpeg_thumbnail_"))
 		{
 			return CloudKey.STATIC_URL + this.user_id + "/" + id + "/" + asset_name + "." + extension;
 		}
 		else
 		{
 			String _url = this.cdn_url + "/route/" + this.user_id + "/" + id + "/" + asset_name + ((!extension.equals("")) ? "." + extension : "");
 			return Helpers.sign_url(_url, this.api_key, seclevel, asnum, ip, useragent, countries, referers, expires) + (download ? "&throttle=0&helper=0&cache=0" : "");
 		}
 	}
 
 	public String mediaCreate() throws Exception
 	{
 		return mediaCreate("");
 	}
 
 	public String mediaCreate(String url) throws Exception
 	{
 		return mediaCreate(url, null, null);
 	}
 
 	public String mediaCreate(String url, DCArray assets_names, DCObject meta) throws Exception
 	{
 		DCObject args = DCObject.create().push("url", url);
 		if (assets_names != null && assets_names.size() > 0)
 		{
 			args.push("assets_names", assets_names);
 		}
 		if (meta != null && meta.size() > 0)
 		{
 			args.push("meta", meta);
 		}
 		DCObject result = this.call("media.create", args);
 		return result.pull("id");
 	}
 
 
 	public String mediaCreate(File f) throws Exception
 	{
 		return this.mediaCreate(f, null, null);
 	}
 
 	public String mediaCreate(File f, DCArray assets_names, DCObject meta) throws Exception
 	{
 		String upload_url = this.fileUpload();
 
 		PostMethod filePost = null;
 		try
 		{
 			filePost = new PostMethod(upload_url);
 
 			Part[] parts = {
 				new FilePart("file", f)
 			};
 
 			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
 			HttpClient client = new HttpClient();
 			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
 
 			int status = client.executeMethod(filePost);
 			if (status == HttpStatus.SC_OK)
 			{
 				ObjectMapper mapper = new ObjectMapper();
 				DCObject json_response = DCObject.create(mapper.readValue(filePost.getResponseBodyAsString(), Map.class));
 				return this.mediaCreate(json_response.pull("url"), assets_names, meta);
 			}
 			else
 			{
 				throw new DCException("Upload failed.");
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DCException("Upload failed.");
 		}
 		finally
 		{
 			if (filePost != null)
 			{
 				filePost.releaseConnection();
 			}
 		}
 	}
 
 	public void mediaDelete(String id) throws Exception
 	{
 		this.call("media.delete", DCObject.create().push("id", id));
 	}
 
 	public String fileUpload() throws Exception
 	{
 		DCObject result = fileUpload(false, "", "");
 		return result.pull("url");
 	}
 
 	public DCObject fileUpload(Boolean status, String jsonp_cb, String target) throws Exception
 	{
 		DCObject args = DCObject.create();
 		if (status)
 		{
 			args.push("status", true);
 		}
 		if (!jsonp_cb.equals(""))
 		{
 			args.push("jsonp_cb", jsonp_cb);
 		}
 		if (!target.equals(""))
 		{
 			args.push("target", target);
 		}
 		return (DCObject) this.call("file.upload", args);
 	}
 }
