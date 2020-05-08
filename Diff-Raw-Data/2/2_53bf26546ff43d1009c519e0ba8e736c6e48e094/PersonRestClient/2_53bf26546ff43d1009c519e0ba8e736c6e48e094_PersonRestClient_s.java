 package no.webstep.person;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.Objects;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.fluent.Request;
 import org.apache.http.entity.ContentType;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 public class PersonRestClient {
     
     private static final int TIMEOUT = 8000;
     private final URI endPoint;
     private final Gson gson;
     
     public PersonRestClient(String endPoint) {
         super();
         try {
             this.endPoint = new URI(endPoint);
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException(e);
         }
         GsonBuilder builder = new GsonBuilder()
                 .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
         gson = builder.create();
     }
     
     private URI urlFor(int personId) {
         return endPoint.resolve("person/" + personId);
     }
     
     private void checkPersonId(Person person) {
         Objects.requireNonNull(person, "person must not be null");
         Objects.requireNonNull(person.getId(), "person.id must not be null");
     }
     
    private Person get(int personId) throws IOException {
         String json = Request.Get(urlFor(personId))
                 .version(HttpVersion.HTTP_1_1)
                 .connectTimeout(TIMEOUT)
                 .socketTimeout(TIMEOUT)
                 .addHeader("accept", ContentType.APPLICATION_JSON.toString())
                 .execute().returnContent().asString();
         return gson.fromJson(json, Person.class);
     }
     
     public Collection<Person> getAll() throws IOException {
         String json = Request.Get(endPoint)
                 .version(HttpVersion.HTTP_1_1)
                 .connectTimeout(TIMEOUT)
                 .socketTimeout(TIMEOUT)
                 .addHeader("accept", ContentType.APPLICATION_JSON.toString())
                 .execute().returnContent().asString();
         
         Collection<Person> persons = gson.fromJson(json, new TypeToken<Collection<Person>>(){}.getType());
         return persons;
     }
     
     public Person create(Person person) throws IOException {
         String json = Request.Post(endPoint)
                 .version(HttpVersion.HTTP_1_1)
                 .connectTimeout(TIMEOUT)
                 .socketTimeout(TIMEOUT)
                 .addHeader("accept", ContentType.APPLICATION_JSON.toString())
                 .bodyString(gson.toJson(person), ContentType.APPLICATION_JSON)
                 .execute().returnContent().asString();
         return gson.fromJson(json, Person.class);
     }
     
     public Person update(Person person) throws IOException {
         checkPersonId(person);
         
         String json = Request.Put(urlFor(person.getId()))
                 .version(HttpVersion.HTTP_1_1)
                 .connectTimeout(TIMEOUT)
                 .socketTimeout(TIMEOUT)
                 .addHeader("accept", ContentType.APPLICATION_JSON.toString())
                 .bodyString(gson.toJson(person), ContentType.APPLICATION_JSON)
                 .execute().returnContent().asString();
         return gson.fromJson(json, Person.class);
     }
     
     public Person delete(Person person) throws IOException {
         checkPersonId(person);
 
         String json = Request.Delete(urlFor(person.getId()))
                 .version(HttpVersion.HTTP_1_1)
                 .connectTimeout(TIMEOUT)
                 .socketTimeout(TIMEOUT)
                 .addHeader("accept", ContentType.APPLICATION_JSON.toString())
                 .execute().returnContent().asString();
         return gson.fromJson(json, Person.class);
     }
     
     public static void main(String[] args) throws Exception {
         
         PersonRestClient client = new PersonRestClient("http://www.fagkomiteen.no/api/person");
         
         Collection<Person> all = client.getAll();
         System.out.println("Fetched " + all.size() + " persons.");
         for (Person p : all) {
             System.out.println(p);
         }
         System.out.println("");
         
         Person brian = client.get(74);
         System.out.println("Fetched " + brian);
         brian.setTitle("Faghelgansvarlig");
         brian = client.update(brian);
         System.out.printf("Updated Brian: %s, title: %s%n", brian, brian.getTitle());
         
         Person newPerson = new Person();
         newPerson.setFirstName("Ada");
         newPerson.setLastName("Lovelace");
         newPerson.setName(newPerson.getFirstName() + " " + newPerson.getLastName());
         newPerson.setEmail("ada.lovelace@webstep.no");
         newPerson.setImageUrl(new URI("http://en.wikipedia.org/wiki/File:Ada_lovelace.jpg"));
         newPerson.setInfo("Augusta Ada King, Countess of Lovelace (10 December 1815 – 27 November 1852), born Augusta Ada Byron and now commonly known as Ada Lovelace, was an English mathematician and writer chiefly known for her work on Charles Babbage's early mechanical general-purpose computer, the analytical engine.");
         newPerson.setInfoUrl(new URI("http://en.wikipedia.org/wiki/Ada_Lovelace"));
         newPerson.setTitle("First Computer Programmer");
         newPerson = client.create(newPerson);
         System.out.println("Created person " + newPerson);
         
         newPerson = client.delete(newPerson);
         System.out.println("Deleted person " + newPerson);
         
     }
 }
