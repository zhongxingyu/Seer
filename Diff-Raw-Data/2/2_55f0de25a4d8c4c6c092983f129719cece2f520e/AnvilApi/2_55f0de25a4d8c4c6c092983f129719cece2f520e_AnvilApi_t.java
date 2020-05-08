 package com.herokuapp.janvil;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import com.sun.jersey.multipart.file.FileDataBodyPart;
 
 import javax.ws.rs.core.MediaType;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.concurrent.Future;
 
 import static com.herokuapp.janvil.CurlFormDataContentDisposition.curlize;
 
 /**
  * @author Ryan Brainard
  */
 class AnvilApi extends AbstractApi {
 
     AnvilApi(Client client, Config config) {
         super(client, config, "api.anvilworks.org");
     }
 
     public Future<ClientResponse> post(Manifest manifest) throws IOException {
         return base
                 .path("/manifest")
                 .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                 .accept(MediaType.APPLICATION_JSON_TYPE)
                 .post(ClientResponse.class, new FormDataMultiPart()
                         .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
     }
 
     public Future<ClientResponse> build(Manifest manifest, Map<String, String> env, String buildpack, String cacheUrl) throws IOException {
         return base
                 .path("/manifest/build")
                 .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                 .accept(MediaType.APPLICATION_JSON_TYPE)
                 .post(ClientResponse.class, new FormDataMultiPart()
                         .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE)
                         .field("env", env, MediaType.APPLICATION_JSON_TYPE)
                         .field("buildpack", buildpack)
                        .field("cache", cacheUrl)
                 );
     }
 
     public Future<ClientResponse> diff(Manifest manifest) throws IOException {
         return base
                 .path("/manifest/diff")
                 .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                 .accept(MediaType.APPLICATION_JSON_TYPE)
                 .post(ClientResponse.class, new FormDataMultiPart()
                         .field("manifest", manifest.getEntries(), MediaType.APPLICATION_JSON_TYPE));
     }
 
     public Future<ClientResponse> post(File file) throws IOException {
         return base
                 .path("/file/" + Manifest.hash(file))
                 .type(MediaType.MULTIPART_FORM_DATA_TYPE)
                 .post(ClientResponse.class, new FormDataMultiPart()
                         .bodyPart(curlize(new FileDataBodyPart("data", file))));
 
     }
 
     public Future<ClientResponse> get(String hash) throws IOException {
         return base
                 .path("/file/" + hash)
                 .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                 .get(ClientResponse.class);
     }
 
     public Future<ClientResponse> get(File file) throws IOException {
         return get(Manifest.hash(file));
     }
 
 }
