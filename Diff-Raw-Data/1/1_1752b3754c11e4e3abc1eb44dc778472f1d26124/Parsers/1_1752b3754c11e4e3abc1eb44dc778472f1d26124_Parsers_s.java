 package controllers;
 
 
 import models.Ingredient;
 import models.Recipe;
 import models.User;
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.PrettyXmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import play.Logger;
 import play.db.jpa.Blob;
 import play.libs.WS;
 import play.libs.XML;
 import play.libs.XPath;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @With(Secure.class)
 public class Parsers extends Controller {
 
     @Before
     static void setConnectedUser() {
         if (Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             user.save();
             if (user != null)
                 renderArgs.put("user", user.fullname);
         }
     }
 
     public static void index() {
         String user = Security.connected();
         render();
     }
 
 
     private static Document getDocument(String url) {
 
         String documentAsString = "<body/>";
         try {
             CleanerProperties props = new CleanerProperties();
 
             TagNode tagNode = htmlCleaner.clean(new URL(url));
 
             // serialize to xml file
             documentAsString = htmlSerializer.getAsString(tagNode);
 
         } catch (Exception e) {
             Logger.error("Kunne ikke hente og xml-balansere side " + url);
         }
         return XML.getDocument(documentAsString);
     }
 
     final static CleanerProperties props = new CleanerProperties();
     final static HtmlCleaner htmlCleaner = new HtmlCleaner(props);
     final static PrettyXmlSerializer htmlSerializer =
             new PrettyXmlSerializer(props);
 
 
     private static void reporter(String report){
 
         response.writeChunk(report + "<br/>");
         Logger.info(report);
     }
 
     public static void importRema1000() {
         String urlTemplate = "http://www.rema.no/under100/?service=oppskrifter&allRecipes=true&page=";
 
         response.setContentTypeIfNotSet("text/html");
         response.writeChunk("<html><body>");
 
         int page = 1;
         boolean morePages = true;
         while (morePages) {
 
             Document recipeDocument = getDocument(urlTemplate + page);
 
             //int oldSize = recipeUrls.size();
             ArrayList<String> recipeUrls = (findRecipeUrls(recipeDocument));
             morePages = recipeUrls.size() > 0;
             page++;
 
             for (final String url : recipeUrls) {
                 Recipe recipe = Recipe.find("bySource", url).first();
                 if (recipe == null) {
                     String result = parseRema1000Recipe(url);
                     response.writeChunk(result + "<br/>");
                 }
             }
 
 
         }
 
 
         importRema1000Tags();
         Application.index();
     }
 
     private static ArrayList<String> findRecipeUrls(Document recipeDocument) {
         ArrayList<String> recipeUrls = new ArrayList<String>();
         for (Node event : XPath.selectNodes("//div[@class='recipeListItem']/div/div/div/a", recipeDocument)) {
             String url = XPath.selectText("@href", event);
             if (url != null && !url.isEmpty()) {
                 recipeUrls.add(url);
                 reporter("Added " + url + " to recipe queue");
             }
         }
         return recipeUrls;
     }
 
     private static void importRema1000Tags() {
         String urlTemplate = "http://www.rema.no/under100/?service=oppskrifter&allRecipes=true&page=1";
         ArrayList<String> tagUrls = new ArrayList<String>();
 
         Document recipeDocument = getDocument(urlTemplate);
 
 
         for (Node event : XPath.selectNodes("//div[@id='tags']/div/ul/li", recipeDocument)) {
             tagRecipes(event);
         }
 
         for (Node event : XPath.selectNodes("//div[@id='campaign_archive_container']/ul/li", recipeDocument)) {
             tagRecipes(event);
         }
 
 
     }
 
     private static void tagRecipes(Node event) {
         String url = XPath.selectText("a/@href", event);
         String name = XPath.selectText("a/span", event);
         List<Node> nameNodes = XPath.selectNodes("a/span", event);
         if(name != null && name.trim().isEmpty())
         {
             name = nameNodes.get(0).getChildNodes().item(2).getTextContent();
         }
         ArrayList<String> tagUrls;
         if (url != null && name != null && !url.isEmpty() && !name.isEmpty()) {
             url = url.replaceAll(" ", "%20");
             name = name.trim();
 
             Document tagDocument = getDocument(url);
 
             tagUrls = findRecipeUrls(tagDocument);
 
             for (String tagUrl : tagUrls) {
                 Recipe recipe = Recipe.find("bySource", tagUrl).first();
                 if (recipe != null) {
                     recipe.tagItWith(name);
                     recipe.save();
                     reporter("Tagget '" + recipe.title + "' med '" + name + "'");
                 }
 
             }
 
         }
     }
 
     private static String parseRema1000Recipe(String url) {
 
         //determine support
 
         // get recipe
         reporter("Retrieveing url: " + url);
         Document recipeDocument = getDocument(url);  //WS.url(url).get().getString();
 
         Recipe recipe = parseRema1000(url, recipeDocument);
 
         if (recipe != null) {
             recipe.save();
 
             return "Successfully parsed " + recipe.title + " from Rema 1000";
         }
         return "Could not parse " + recipe.title + " from Rema 1000";
     }
 
     private static Recipe parseRema1000(String url, Document recipeDocument) {
 
 
         String title = XPath.selectText("//div[@id='recipe']/div[@class='rightColumn']/h2", recipeDocument);
         title = title.replaceAll(" +\t+ +\n+ +", " ");
 
 
         String description = XPath.selectText("//div[@id='recipe']/div[@class='leftColumn']/div[@id='ingress']/p", recipeDocument);
 
         String steps = "";
 
         for (Node event : XPath.selectNodes("//div[@id='recipe']/div[@class='leftColumn']/div[@id='fremgangsmote']/div[@class='stepByStepItem']", recipeDocument)) {
 
             steps = steps + getStep(event);
         }
         for (Node event : XPath.selectNodes("//div[@id='recipe']/div[@class='leftColumn']/div[@id='fremgangsmote']/div[@class='stepByStepItem_noImage']", recipeDocument)) {
 
             steps = steps + getStep(event);
         }
 
 
         String source = url;
 
         double serves = 2;
         try {
 
             String adultRangeString = XPath.selectText("//div[@id='recipe']/div[@class='rightColumn']/div/span[@id='adultRange']", recipeDocument);
             String childRangeString = XPath.selectText("//div[@id='recipe']/div[@class='rightColumn']/div/span[@id='childRange']", recipeDocument);
 
             serves = Integer.parseInt(adultRangeString) + Integer.parseInt(childRangeString) / 2;
 
         } catch (NumberFormatException e) {
             //
         }
 
         String servesUnit = "personer";
 
 
         User user = User.find("byEmail", Security.connected()).first();
         Recipe recipe = new Recipe(user, title, description, steps, source, serves, servesUnit).save();
 
         for (Node event : XPath.selectNodes("//div[@id='recipe']/div[@class='rightColumn']/div/div[@class='ingredient_parent']", recipeDocument)) {
 
             String amount = "0";
             String unit = "";
             String ingredientName = "";
 
             String mengdeEnhet = XPath.selectText("div[@class='mengdeEnhet']", event);
             if (mengdeEnhet != null) {
                 String[] mengdeEnhetArray = mengdeEnhet.trim().split(" ");
                 amount = mengdeEnhetArray[0];
                 unit = mengdeEnhetArray[1];
                 if(amount!=null)
                 {
                     amount = amount.replace(",",".");
                 }
             }
 
             String produktnavn = XPath.selectText("div[@class='produktnavn']", event);
             if (produktnavn != null) {
                 ingredientName = cleanProductNames(produktnavn);
             }
 
             recipe.addIngredient(amount, unit, ingredientName);
             convertPackageToWeight(recipe.ingredients.get(recipe.ingredients.size()-1));
         }
 
         String tidsbruk = XPath.selectText("//div[@class='tidsbruk']/strong", recipeDocument);
      //   recipe.tagItWith(tidsbruk + "min");
      //   recipe.tagItWith("rema1000");
 
         String photoUrl;
         photoUrl = XPath.selectText("//div[@id='recipe']/div[@class='leftColumn']/div[@class='recipeImage recipeImageDraggable']/img/@src", recipeDocument);
 
         Blob photo = new Blob();
 
         WS.HttpResponse response = WS.url(photoUrl).get();
         InputStream fileStream = response.getStream();
         photo.set(fileStream, response.getContentType());
         recipe.addPhoto(photo);
 
         return recipe;
     }
 
     private static Pattern packagePattern =  Pattern.compile("c?a? ?([0-9]+,?[0-9]*) ?(k?g)");
 
     private static void convertPackageToWeight(Ingredient originalIngredient)
     {
         if(originalIngredient.unit == "pk")
         {
              Matcher matcher = packagePattern.matcher(originalIngredient.description);
             if(matcher.find())
             {
                 String amount = matcher.group(1);
                 String newDesc = matcher.replaceFirst("");
                 String unit = matcher.group(2);
                 Double amountDouble = Double.parseDouble(amount);
                 Double totalAmount = amountDouble*Double.parseDouble(originalIngredient.amount);
 
                 Logger.info("Replacing" + originalIngredient.amount + " " + originalIngredient.unit + " " + originalIngredient.description + " with: " + totalAmount + " " + unit + " " + newDesc );
             }
         }
     }
 
     private static String cleanProductNames(String produktnavn) {
         String[] kjenteProdukter = new String[]{"Godehav", "Solvinge", "REMA 1000", "Tine", "Bama", "Kikkoman", "Nordfjord", "Blue Dragon", "Taga", "Finsbråten", "Hatting", "Mesterbakeren", "Grilstad", "Ideal", "Staur", "MaxMat", "Viddas", "frossen", " - NB! Sesongvare"};
 
         for(String kjentProdukt:kjenteProdukter)
         {
             produktnavn = produktnavn.replaceAll(" *" + kjentProdukt + " *", "");
         }
         return produktnavn.trim();
     }
 
     private static String getStep(Node event) {
         String steps = "";
         String overskrift = XPath.selectText("h4", event);
         if (overskrift != null) {
             steps = steps + overskrift + "\n\n";
         }
 
         String tekst = XPath.selectText("p", event);
         if (tekst != null) {
             steps = steps + tekst + "\n\n";
         }
         return steps;
     }
 
     private static Document cleanRema1000Html(String content) {
         String bodyContent = content.substring(content.indexOf("<body>"), content.indexOf("</body>") + 7);
 
         bodyContent = bodyContent.replaceAll("<input[^>]*>", "");
 
         bodyContent = bodyContent.replaceAll("^\\t* *\\t* *$\\n", "");
 
         bodyContent = bodyContent.substring(0, bodyContent.indexOf("<script")) + bodyContent.substring(bodyContent.indexOf("</script>") + 9);
 
         bodyContent = bodyContent.replaceAll("&amp;", "&");
         bodyContent = bodyContent.replaceAll("&", "&amp;");
 
         return XML.getDocument(bodyContent);
     }
 
 
 }
