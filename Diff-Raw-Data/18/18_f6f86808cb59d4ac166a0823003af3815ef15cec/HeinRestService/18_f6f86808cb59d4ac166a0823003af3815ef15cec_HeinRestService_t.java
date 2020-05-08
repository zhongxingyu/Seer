 package net.binout.codestory2013;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Stopwatch;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.io.CharStreams;
 import com.google.gson.Gson;
 import com.petebevin.markdown.MarkdownProcessor;
 import net.binout.codestory2013.calculator.RhinoCalculator;
 import net.binout.codestory2013.scalaskel.Scalaskel;
 import net.binout.codestory2013.scalaskel.ScalaskelResult;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 
 @Path("/")
 @ApplicationScoped
 public class HeinRestService {
 
     static final String HEIN_BINOUT = "Hein binout?";
     static final String Q = "q";
     static final String QUELLE_EST_TON_ADRESSE_EMAIL = "Quelle est ton adresse email";
     static final String MAIL = "binout@gmail.com";
     static final String ES_TU_ABONNE_A_LA_MAILING_LIST_OUI_NON = "Es tu abonne a la mailing list(OUI/NON)";
     static final String ES_TU_HEUREUX_DE_PARTICIPER_OUI_NON = "Es tu heureux de participer(OUI/NON)";
     static final String ES_TU_PRET_A_RECEVOIR_UNE_ENONCE_AU_FORMAT_MARKDOWN_PAR_HTTP_POST_OUI_NON = "Es tu pret a recevoir une enonce au format markdown par http post(OUI/NON)";
     static final String EST_CE_QUE_TU_REPONDS_TOUJOURS_OUI_OUI_NON = "Est ce que tu reponds toujours oui(OUI/NON)";
     static final String OUI = "OUI";
     static final String NON = "NON";
     public static final String AS_TU_BIEN_RECU_LE_PREMIER_ENONCE_OUI_NON = "As tu bien recu le premier enonce(OUI/NON)";
 
     static final int MIN_SCALASKEL = 1;
     static final int MAX_SCALASKEL = 100;
     static final String SCALASKEL_NO_CACHE_LOADING_PROPERTY = "scalaskel.nocacheloading";
 
     List<String> enonces;
     LoadingCache<Integer, String> cache;
     RhinoCalculator calculator;
 
     public HeinRestService() {
         enonces = new ArrayList<String>();
         try {
             InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("scalaskel.txt");
             String scalaskelEnonce = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
             enonces.add(scalaskelEnonce);
         } catch (IOException e) {
             System.out.println("Cannot load enonces...");
         }
 
         final Scalaskel scalaskel = new Scalaskel();
         final Gson gson = new Gson();
         cache = CacheBuilder.newBuilder().maximumSize(MAX_SCALASKEL).build(new CacheLoader<Integer, String>() {
             @Override
             public String load(Integer key) throws Exception {
                 Set<ScalaskelResult> result = scalaskel.change(key);
                 return gson.toJson(result);
             }
         });
 
         calculator = new RhinoCalculator();
     }
 
     @PostConstruct
     public void initScalaskelCache() {
         if ("true".equals(System.getProperty(SCALASKEL_NO_CACHE_LOADING_PROPERTY))) {
             System.out.println("No cache, faster test");
         } else {
             Stopwatch sw = new Stopwatch();
             sw.start();
             System.out.println("Loading cache");
             for(int i=MIN_SCALASKEL; i<=MAX_SCALASKEL; i++) {
                 cache.getUnchecked(i);
             }
             System.out.println("Cache loaded in : " + sw);
         }
     }
 
     private Response badRequest() {
         return Response.status(Response.Status.BAD_REQUEST).entity(HEIN_BINOUT).build();
     }
 
     @GET
     public Response get(@Context UriInfo uriInfo, @QueryParam(Q) String query) {
         System.out.println("\nGET");
         logAllQueryParameters(uriInfo);
 
         if (QUELLE_EST_TON_ADRESSE_EMAIL.equals(query)) {
             return Response.ok(MAIL).build();
         }
         if (ES_TU_ABONNE_A_LA_MAILING_LIST_OUI_NON.equals(query)
                 || ES_TU_HEUREUX_DE_PARTICIPER_OUI_NON.equals(query)
                 || ES_TU_PRET_A_RECEVOIR_UNE_ENONCE_AU_FORMAT_MARKDOWN_PAR_HTTP_POST_OUI_NON.equals(query)
                 || AS_TU_BIEN_RECU_LE_PREMIER_ENONCE_OUI_NON.equals(query)) {
             return Response.ok(OUI).build();
         }
         if (EST_CE_QUE_TU_REPONDS_TOUJOURS_OUI_OUI_NON.equals(query)) {
             return Response.ok(NON).build();
         }
         if (query != null && query.matches(".*\\d.*")) {
             String expression = parseMathQuery(query);
             try {
                 String result = calculator.calculate(expression);
                 result = toMathFrench(result);
                 System.out.println(expression + " = " + result);
                 return Response.ok(result).build();
             } catch (Exception e) {
                 System.out.println(expression + " = " + e.getMessage());
                 return badRequest();
             }
         }
         return badRequest();
     }
 
     String toMathFrench(String exp) {
         String result = exp;
         double d = Double.parseDouble(result);
         int i = (int) d;
         if (i == d) {
             // if number is entire, remove all characters after .
             int index = result.indexOf('.');
             if (index != -1) {
                 result = result.substring(0, index);
             }
         }
         result = result.replaceAll("\\.", ",");
         return result;
     }
 
     private String parseMathQuery(String query) {
        String result = query.replaceAll(" ", "+");
        return result.replaceAll(",", "\\.");
     }
 
     private void logAllQueryParameters(UriInfo uriInfo) {
         MultivaluedMap<String,String> queryParameters = uriInfo.getQueryParameters();
         Set<Map.Entry<String,List<String>>> entries = queryParameters.entrySet();
         for (Map.Entry<String,List<String>> entry : entries) {
             System.out.println("Parameter : " + entry.getKey());
             for (String value : entry.getValue()) {
                 System.out.println("Value : " + value);
             }
         }
     }
 
     @GET
     @Path("/enonce")
     public String getEnonces() {
         StringBuilder builder = new StringBuilder();
         builder.append("<hr/>");
         MarkdownProcessor m = new MarkdownProcessor();
         for(String enonce : enonces) {
             builder.append(m.markdown(enonce));
             builder.append("<hr/>");
         }
         return builder.toString();
     }
 
     @POST
     @Path("/enonce/{id}")
     public Response postEnonce(@PathParam("id") String id, String message) {
         System.out.println("\nPOST : Enonce" + id);
         enonces.add(message);
         System.out.println(message);
         return Response.status(Response.Status.CREATED).build();
     }
 
     @GET
     @Path("scalaskel/change/{X}")
     public Response getScalaskel(@PathParam("X") String number) {
         System.out.println("\nGET : scalaskel/change/" + number);
         int toChange;
         try {
             toChange = Integer.valueOf(number);
         } catch (NumberFormatException e) {
             return badRequest();
         }
         if (toChange<MIN_SCALASKEL || toChange>MAX_SCALASKEL) {
             return badRequest();
         }
         String json = cache.getUnchecked(toChange);
         return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
     }
 
 }
