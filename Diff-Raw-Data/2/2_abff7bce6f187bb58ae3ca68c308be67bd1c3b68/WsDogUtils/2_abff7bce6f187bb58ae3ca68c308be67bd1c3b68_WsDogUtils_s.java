 package mx.ferreyra.dogapp;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Map;
 
 import mx.ferreyra.dogapp.org.ksoap2.SoapEnvelope;
 import mx.ferreyra.dogapp.org.ksoap2.serialization.SoapObject;
 import mx.ferreyra.dogapp.org.ksoap2.serialization.SoapSerializationEnvelope;
 import mx.ferreyra.dogapp.org.ksoap2.transport.HttpTransportSE;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 
 /**
  * @author Israel Buitron
  */
 public class WsDogUtils {
 
     public Context context;
     private String url = "http://marketing7veinte.net/dc_app_perroton/appWSDog/wsDog.asmx?WSDL";
     private String namespace = "http://tempuri.org/";
 
     private static final String EDIT_DUENO_MASCOTA = "editDuenoMascota";
     private static final String GET_CAT_ACTIVIDAD_FISICA = "getCatActividadFisica";
     private static final String GET_CAT_ESTADOS = "getCatEstados";
     private static final String GET_CAT_GENERO = "getCatGenero";
     private static final String GET_CAT_TIPO_VIDA = "getCatTipoVida";
     private static final String GET_DUENOS_MASCOTAS = "getDuenosMascotas";
     private static final String GET_DUENOS_MASCOTAS_BY_ID_USUARIO = "getDuenosMascotasByIdUsuario";
     private static final String GET_TIPS_BY_ID_USUARIO = "getTipsByIdUsuario";
     private static final String GET_TRAINING_SPOT = "getTrainingSpot";
     private static final String INSERT_IPHONE_ID = "insertIphoneID";
     private static final String INSERT_RATING = "insertRating";
     private static final String INSERT_ROUTE = "insertRoute";
     private static final String INSERT_USER_IPHONE = "insertUserIphone";
     private static final String INSERT_USERS = "insertUsers";
     private static final String USER_LOGIN = "userLogin";
     private static final String USER_RECOVERY_PWD = "userRecoveryPWD";
 
     public WsDogUtils(Context context) {
         this.context = context;
     }
 
     public Integer editDuenoMascota(Map parameters)
         throws IOException, XmlPullParserException {
         return genericDuenoMascota(EDIT_DUENO_MASCOTA,
                                    namespace + EDIT_DUENO_MASCOTA,
                                    parameters);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1","Moderada"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdActividadFisica&gt;1&lt;/IdActividadFisica&gt;
      * &lt;Descripcion&gt;Moderada&lt;/Descripcion&gt;
      * </code></p>
      */
     public String[][] parseGetCatActividadFisica(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 2);
     }
 
     public String[][] getCatActividadFisica()
         throws IOException, XmlPullParserException {
         SoapObject result = (SoapObject)genericRequest(GET_CAT_ACTIVIDAD_FISICA,
                                                        namespace + GET_CAT_ACTIVIDAD_FISICA,
                                                        new SoapObject(namespace, GET_CAT_ACTIVIDAD_FISICA));
         return parseGetCatActividadFisica(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1","Aguascalientes"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdEstado&gt;1&lt;/IdEstado&gt;
      * &lt;Descripcion&gt;Aguascalientes&lt;/Descripcion&gt;
      * </code></p>
      */
     public String[][] parseGetCatEstados(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 2);
     }
 
     public String[][] getCatEstados()
         throws IOException, XmlPullParserException {
         SoapObject result = (SoapObject)genericRequest(GET_CAT_ESTADOS,
                                                        namespace + GET_CAT_ESTADOS,
                                                        new SoapObject(namespace, GET_CAT_ESTADOS));
         return parseGetCatEstados(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1","Masculino","Macho"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdGenero&gt;1&lt;/IdGenero&gt;
      * &lt;Descripcion&gt;Masculino&lt;/Descripcion&gt;
      * &lt;Descripcion2&gt;Macho&lt;/Descripcion2&gt;
      * </code></p>
      */
     public String[][] parseGetCatGenero(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 3);
     }
 
     public String[][] getCatGenero()
         throws IOException, XmlPullParserException {
         SoapObject result = (SoapObject)genericRequest(GET_CAT_GENERO,
                                                        namespace + GET_CAT_GENERO,
                                                        new SoapObject(namespace, GET_CAT_GENERO));
         return parseGetCatGenero(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1","Exterior"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdTipoVida&gt;1&lt;/IdTipoVida&gt;
      * &lt;Descripcion&gt;Exterior&lt;/Descripcion&gt;
      * </code></p>
      */
     public String[][] parseGetCatTipoVida(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 17);
     }
 
     public String[][] getCatTipoVida()
         throws IOException, XmlPullParserException {
         SoapObject result = (SoapObject)genericRequest(GET_CAT_TIPO_VIDA,
                                                        namespace + GET_CAT_TIPO_VIDA,
                                                        new SoapObject(namespace, GET_CAT_TIPO_VIDA));
         return parseGetCatTipoVida(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["14","Israel","1","2012-08-09","6","Balam","Sharpei","2","2",
      *   "2012-09-09","/9j/....",null,null,"2012-09-10","Mientras..."]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdDueno&gt;14&lt;/IdDueno&gt;
      * &lt;IdUsuario&gt;10&lt;/IdUsuario&gt;
      * &lt;DuenoNombre&gt;Israel&lt;/DuenoNombre&gt;
      * &lt;DuenoIdGenero&gt;1&lt;/DuenoIdGenero&gt;
      * &lt;DuenoFechaCumpleanos&gt;2012-08-09&lt;/DuenoFechaCumpleanos&gt;
      * &lt;DuenoIdEstado&gt;6&lt;/DuenoIdEstado&gt;
      * &lt;MascotaNombre&gt;Balam&lt;/MascotaNombre&gt;
      * &lt;MascotaRaza&gt;Sharpei&lt;/MascotaRaza&gt;
      * &lt;MascotaIdGenero&gt;2&lt;/MascotaIdGenero&gt;
      * &lt;MascotaIdTipoVida&gt;2&lt;/MascotaIdTipoVida&gt;
      * &lt;MascotaIdActividadFisica&gt;2&lt;/MascotaIdActividadFisica&gt;
      * &lt;MascotaFechaCumpleanos&gt;2012-09-09&lt;/MascotaFechaCumpleanos&gt;
      * &lt;MascotaImagen&gt;/9j/...&lt;/MascotaImagen&gt;
      * &lt;Comentarios1/&gt;
      * &lt;Comentarios2/&gt;
      * &lt;FechaRegistro&gt;2012-09-10&lt;/FechaRegistro&gt;
      * &lt;Tip&gt;Mientras...&lt;/Tip&gt;
      * </code></p>
      */
     public String[][] parseGetDuenosMascotas(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 17);
     }
 
     public String[][] getDuenosMascotas()
         throws IOException, XmlPullParserException {
         SoapObject result = (SoapObject)genericRequest(GET_DUENOS_MASCOTAS,
                                                        namespace + GET_DUENOS_MASCOTAS,
                                                        new SoapObject(namespace, GET_DUENOS_MASCOTAS));
         return parseGetDuenosMascotas(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["14","Israel","1","2012-08-09","6","Balam","Sharpei","2","2",
      *   "2012-09-09","/9j/....",null,null,"2012-09-10","Mientras..."]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdDueno&gt;14&lt;/IdDueno&gt;
      * &lt;IdUsuario&gt;10&lt;/IdUsuario&gt;
      * &lt;DuenoNombre&gt;Israel&lt;/DuenoNombre&gt;
      * &lt;DuenoIdGenero&gt;1&lt;/DuenoIdGenero&gt;
      * &lt;DuenoFechaCumpleanos&gt;2012-08-09&lt;/DuenoFechaCumpleanos&gt;
      * &lt;DuenoIdEstado&gt;6&lt;/DuenoIdEstado&gt;
      * &lt;MascotaNombre&gt;Balam&lt;/MascotaNombre&gt;
      * &lt;MascotaRaza&gt;Sharpei&lt;/MascotaRaza&gt;
      * &lt;MascotaIdGenero&gt;2&lt;/MascotaIdGenero&gt;
      * &lt;MascotaIdTipoVida&gt;2&lt;/MascotaIdTipoVida&gt;
      * &lt;MascotaIdActividadFisica&gt;2&lt;/MascotaIdActividadFisica&gt;
      * &lt;MascotaFechaCumpleanos&gt;2012-09-09&lt;/MascotaFechaCumpleanos&gt;
      * &lt;MascotaImagen&gt;/9j/...&lt;/MascotaImagen&gt;
      * &lt;Comentarios1/&gt;
      * &lt;Comentarios2/&gt;
      * &lt;FechaRegistro&gt;2012-09-10&lt;/FechaRegistro&gt;
      * &lt;Tip&gt;Mientras...&lt;/Tip&gt;
      * </code></p>
      */
     public String[][] parseGetDuenosMascotasByIdUsuario(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 17);
     }
 
     public String[][] getDuenosMascotasByIdUsuario(Integer userId)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, GET_DUENOS_MASCOTAS_BY_ID_USUARIO);
        request.addProperty("userId", userId);
         SoapObject result = (SoapObject)genericRequest(GET_DUENOS_MASCOTAS_BY_ID_USUARIO,
                                                        namespace + GET_DUENOS_MASCOTAS_BY_ID_USUARIO,
                                                        request);
         return parseGetDuenosMascotas(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["0","0","NaN"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;Distance&gt;0&lt;/Distance&gt;
      * &lt;SecondTime&gt;0&lt;/SecondTime&gt;
      * &lt;Speed&gt;NaN&lt;/Speed&gt;
      * </code></p>
      */
     public String[][] parseGetStats(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 3);
     }
 
     public String[][] getStats(Map parameters)
         throws IOException, XmlPullParserException {
         String method = "getStats";
         SoapObject request = new SoapObject(namespace, method);
         request.addProperty("userId",(Integer)parameters.get("user_id"));
         SoapObject result = (SoapObject)genericRequest(method,
                                                        "http://tempuri.org/getStats",
                                                        request);
         return parseGetStats(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1","Evita..."]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;IdTip&gt;1&lt;/IdTip&gt;
      * &lt;Descripcion&gt;Evita...&lt;/Descripcion&gt;
      * </code></p>
      */
     public String[][] parseGetTipsByIdUsuario(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 2);
     }
 
     public String[][] getTipsByIdUsuario(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, GET_TIPS_BY_ID_USUARIO);
         request.addProperty("idUsuario",(Integer)parameters.get("user_id"));
         SoapObject result = (SoapObject)genericRequest(GET_TIPS_BY_ID_USUARIO,
                                                        namespace + GET_TIPS_BY_ID_USUARIO,
                                                        request);
         return parseGetTipsByIdUsuario(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      */
     public String[][] parseGetTrainingSpot(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 11);
     }
 
     public String[][] getTrainingSpot(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, GET_TRAINING_SPOT);
         request.addProperty("latitude",(String)parameters.get("latitude"));
         request.addProperty("longitude",(String)parameters.get("longitude"));
         SoapObject result = (SoapObject)genericRequest(GET_TRAINING_SPOT,
                                                        namespace + GET_TRAINING_SPOT,
                                                        request);
         return parseGetTrainingSpot(result);
     }
 
     public Integer insertDuenoMascota(Map parameters)
         throws IOException, XmlPullParserException {
         return genericDuenoMascota("insertDuenoMascota",
                                    "http://tempuri.org/insertDuenoMascota",
                                    parameters);
     }
 
     public Integer genericDuenoMascota(String method, String action, Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, method);
         request.addProperty("idUsuario",(String)parameters.get("idUsuario"));
         request.addProperty("duenoNombre",(String)parameters.get("duenoNombre"));
         request.addProperty("duenoIdGenero",(String)parameters.get("duenoIdGenero"));
         request.addProperty("duenoFechaCumpleanos",(String)parameters.get("duenoFechaCumpleanos"));
         request.addProperty("duenoIdEstado",(String)parameters.get("duenoIdEstado"));
         request.addProperty("mascotaNombre",(String)parameters.get("mascotaNombre"));
         request.addProperty("mascotaRaza",(String)parameters.get("mascotaRaza"));
         request.addProperty("mascotaIdGenero",(String)parameters.get("mascotaIdGenero"));
         request.addProperty("mascotaIdTipoVida",(String)parameters.get("mascotaIdTipoVida"));
         request.addProperty("mascotaFechaCumpleanos",(String)parameters.get("mascotaFechaCumpleanos"));
         request.addProperty("mascotaIdActividadFisica",(String)parameters.get("mascotaIdActividadFisica"));
         request.addProperty("mascotaImagen",(String)parameters.get("mascotaImagen"));
         request.addProperty("comentarios1",(String)parameters.get("comentarios1"));
         request.addProperty("comentarios2",(String)parameters.get("comentarios2"));
 
         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
         envelope.setOutputSoapObject(request);
         envelope.dotNet = true;
 
         HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
         androidHttpTransport.debug = true;
         androidHttpTransport.call(action, envelope);
 
         if((envelope.bodyIn) instanceof SoapObject) {
             // Soap object response
             return parseGenericReturnSoapObject((SoapObject)envelope.bodyIn);
         } else {
             // Soap fault response
             return null;
         }
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1|37"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;1|37&lt;/return&gt;
      * </code></p>
      */
     public String[][] parseInsertIphoneID(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 1);
     }
 
     public String[][] insertIphoneID(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, INSERT_IPHONE_ID);
         request.addProperty("IphoneID",(Integer)parameters.get("iphone_id"));
         SoapObject result = (SoapObject)genericRequest(INSERT_IPHONE_ID,
                                                        namespace + INSERT_IPHONE_ID,
                                                        request);
         return parseInsertIphoneID(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;1&lt;/return&gt;
      * </code></p>
      */
     public String[][] parseInsertRating(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 1);
     }
 
     public String[][] insertRating(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, INSERT_RATING);
         request.addProperty("routeId",(Integer)parameters.get("route_id"));
         request.addProperty("rating",(String)parameters.get("rating"));
         SoapObject result = (SoapObject)genericRequest(INSERT_RATING,
                                                        namespace + INSERT_RATING,
                                                        request);
         return parseInsertRating(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["27"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;27&lt;/return&gt;
      * </code></p>
      */
     public String[][] parseInsertRoute(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 1);
     }
 
     public String[][] insertRoute(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, INSERT_ROUTE);
         request.addProperty("routeName",(String)parameters.get("route_name"));
         request.addProperty("sourceLatitude",(String)parameters.get("source_latitude"));
         request.addProperty("sourceLongitude",(String)parameters.get("source_longitude"));
         request.addProperty("routeLatitude",(String)parameters.get("route_latitude"));
         request.addProperty("routeLongitude",(String)parameters.get("route_longitude"));
         request.addProperty("distance",(String)parameters.get("distance"));
         request.addProperty("timeTaken",(String)parameters.get("time_taken"));
         request.addProperty("difficulty",(String)parameters.get("difficulty"));
         request.addProperty("userId",(Integer)parameters.get("user_id"));
         SoapObject result = (SoapObject)genericRequest(INSERT_ROUTE,
                                                        namespace + INSERT_ROUTE,
                                                        request);
         return parseInsertRoute(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;1&lt;/return&gt;
      * </code></p>
      */
     public String[][] parseInsertUserIphone(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 1);
     }
 
     public String[][] insertUserIphone(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, INSERT_USER_IPHONE);
         request.addProperty("IphoneID",(String)parameters.get("iphone_id"));
         request.addProperty("username",(String)parameters.get("username"));
         request.addProperty("password",(String)parameters.get("password"));
         request.addProperty("isFacebook",(String)parameters.get("is_facebook"));
         SoapObject result = (SoapObject)genericRequest(INSERT_USER_IPHONE,
                                                        namespace + INSERT_USER_IPHONE,
                                                        request);
         return parseInsertUserIphone(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["1"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;1&lt;/return&gt;
      * </code></p>
      */
     public Integer parseInsertUsers(SoapObject result) {
         return parseGenericReturnSoapObject(result);
     }
 
     public Integer insertUsers(String username, String password, boolean isFacebook)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, INSERT_USERS);
         request.addProperty("username",username);
         request.addProperty("password",password);
         request.addProperty("isFacebook",isFacebook ? "1" : "0");
         SoapObject result = (SoapObject)genericRequest(INSERT_USERS,
                                                        namespace + INSERT_USERS,
                                                        request);
         return parseInsertUsers(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["38"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;38&lt;/return&gt;
      * </code></p>
      */
     public Integer parseUserLogin(SoapObject result) {
         return parseGenericReturnSoapObject(result);
     }
 
     public Integer userLogin(String username, String password)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, USER_LOGIN);
         request.addProperty("username",username);
         request.addProperty("password",password);
 
         SoapObject result = (SoapObject)genericRequest(USER_LOGIN,
                                                        namespace + USER_LOGIN,
                                                        request);
         return parseUserLogin(result);
     }
 
     /**
      * Parse soap object response.
      * @param result Soap object response to parse.
      * @return If <code>result</code> parameter it returns <code>null</code>,
      * otherwise return <code>String</code> matrix with elements parsed.
      * This matrix should look like:
      * <p><code>
      * [["SERVER_ERROR"]]
      * </code></p>
      * for this soap response:
      * <p><code>
      * &lt;return&gt;SERVER_ERROR&lt;/return&gt;
      * </code></p>
      */
     public String[][] parseUserRecoveryPWD(SoapObject result) {
         return parseGenericMatrixSoapObject(result, 1);
     }
 
     public String[][] userRecoveryPWD(Map parameters)
         throws IOException, XmlPullParserException {
         SoapObject request = new SoapObject(namespace, USER_RECOVERY_PWD);
         request.addProperty("username",(String)parameters.get("username"));
         SoapObject result = (SoapObject)genericRequest(USER_RECOVERY_PWD,
                                                        namespace + USER_RECOVERY_PWD,
                                                        request);
         return parseUserRecoveryPWD(result);
     }
 
     public Integer parseGenericReturnSoapObject(SoapObject result) {
         if (result == null)
             return null;
 
         SoapObject root = (SoapObject)result.getProperty(0);
         SoapObject diffgram = (SoapObject)root.getProperty(1);
         SoapObject dataSet = (SoapObject)diffgram.getProperty(0);
         SoapObject table = (SoapObject)dataSet.getProperty(0);
         return Integer.valueOf(table.getPropertyAsString(0));
     }
 
     public String[][] parseGenericMatrixSoapObject(SoapObject result, int columns) {
         if (result == null)
             return null;
 
         SoapObject root = (SoapObject)result.getProperty(0);
         SoapObject diffgram = (SoapObject)root.getProperty(1);
 	if(diffgram.getPropertyCount()==0) {
 	    // No data returned
 	    return null;
 	}
 
         SoapObject dataSet = (SoapObject)diffgram.getProperty(0);
         int count = dataSet.getPropertyCount();
         String[][] values = new String[count][columns];
 
         for(int i=0; i<count; i++) {
             SoapObject table = (SoapObject)dataSet.getProperty(i);
             for(int j=0; j<columns; j++)
                 values[i][j] = table.getPropertyAsString(j);
         }
 
         return values;
     }
 
 
     public Object genericRequest(String method,
                                  String action,
                                  SoapObject request)
         throws IOException, XmlPullParserException {
         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
         envelope.setOutputSoapObject(request);
         envelope.dotNet = true;
 
         HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
         androidHttpTransport.debug = true;
         androidHttpTransport.call(action, envelope);
 
         return envelope.bodyIn;
     }
 }
