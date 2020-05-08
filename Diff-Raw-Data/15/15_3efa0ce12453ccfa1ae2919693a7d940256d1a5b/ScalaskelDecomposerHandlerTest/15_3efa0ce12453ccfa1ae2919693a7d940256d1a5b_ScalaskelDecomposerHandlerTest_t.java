 package cs13;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.ClassRule;
 import org.junit.Test;
 import us.monoid.json.JSONException;
 import us.monoid.web.Resty;
 
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  *
  *  Serveur Web
  *
  *  Votre serveur doit répondre aux requetes http GET de la forme `http://serveur/scalaskel/change/X`,
  *  `X` étant une valeur en cents de 1 à 100 cents.
  *
  *  La réponse attendue est un json de la forme : [{“foo”: w, “bar”, x, “qix”, y, “baz”: z}, …]
  *
  *  Exemples Pour `http://serveur/scalaskel/change/1` il faut répondre : [ {“foo”: 1} ]
  *  Pour `http://serveur/scalaskel/change/7` il faut répondre : [ {“foo”: 7}, {“bar”, 1} ]
  *
  *  L’ordre des valeurs dans le tableau json, ainsi que le formatage n’a pas d’importance
  *  à partir du moment ou c’est du json valide, il s’entends.
  *
  *
  *  Nous ne faisons pas trop de tests sur les valeurs ici, les tests de decomposition sont fait sur
  *  le decomposer lui meme.
  */
 public class ScalaskelDecomposerHandlerTest {
 
     @ClassRule
     public static ServerRule server = new ServerRule();
 
     @Test
     public void should_decompose_1() throws IOException, JSONException {
         JsonNode json = doGetChangeAndCheckResultIsArray(1);
 
         assertThat(json.size(), is(1));
         assertThat(json.get(0).isObject(), is(true));
         assertThat(json.get(0).get("foo").asInt(), is(1));
     }
 
     @Test
     public void should_decompose_7() throws IOException, JSONException {
         JsonNode json = doGetChangeAndCheckResultIsArray(7);
 
         assertThat(json.size(), is(2));
         assertThat(json.get(1).isObject(), is(true));
         assertThat(json.get(1).get("foo").asInt(), is(7));
         assertThat(json.get(0).isObject(), is(true));
         assertThat(json.get(0).get("bar").asInt(), is(1));
     }
 
     @Test
     public void should_decompose_89() throws IOException, JSONException {
         JsonNode json = doGetChangeAndCheckResultIsArray(89);
         assertThat(json.size(), is(132));
     }
 
     private JsonNode doGetChangeAndCheckResultIsArray(int groDecimaux) throws IOException {
         // resty json support is not able to return json array directly, only json object :(
         String s = new Resty().text(
                 server.uriBuilder()
                        .withPath("/scalaskel/change/" + groDecimaux)
                         .toUri()).toString();
         JsonNode json = new ObjectMapper().readTree(s);
 
         assertThat(json, is(notNullValue()));
         assertThat(json.isArray(), is(true));
         return json;
     }
 
 }
