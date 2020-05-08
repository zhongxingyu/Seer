 package net.aequologica.parimagine.model;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.aequologica.parimagine.utils.RequestUtils;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.fr.FrenchAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Predicate;
 import com.sap.prd.geppaequo.config.ConfigRegistry;
 import com.sap.prd.parimagine.config.ParimagineConfig;
 
 public class Photos {
     
     private static Logger log = LoggerFactory.getLogger(Photos.class);
     
     private static Photos instance = null;
 
     public static Photos getInstance() throws IOException {
         if (Photos.instance == null) {
             Photos.instance = new Photos();
         }
         return Photos.instance;
     }
 
     final ArrayList<Photo> list; // toutes les photos, must be array list
     final Map<String, Photo> image2photoMap; 
     final Map<Integer, List<Photo>> districtLists = new HashMap<>();
     final Map<String, List<Photo>> themeLists = new HashMap<>();
     
     final String documents;
     final Boolean verify;
 
     // must be sorted ! (used in binary search)
     String[] themes = new String[] {
     	    "bals",
     	    "cinema",
     	    "enfants",
     	    "enseignes",
     	    "etudiants",
     	    "fetesaintecatherine",
     	    "halles",
     	    "manifestations",
     	    "metiers",
     	    "police",
     	    "pompiers",
     	    "quais",
     	    "saisons",
     	    "vie-montmartre"
     };
     
     class PredicateDistrict implements Predicate<Photo> {
         
         private Integer district;
 
         private PredicateDistrict(Integer district) {
             super();
             this.district = district;
         }
 
         @Override
         public boolean apply(Photo photo) {
             return district.equals(photo.getAddress().getDistrict());
         }
     }
     
     class PredicateTheme implements Predicate<Photo> {
         
         private String theme;
 
         private PredicateTheme(String theme) {
             super();
             this.theme = theme;
         }
 
         @Override
         public boolean apply(Photo photo) {
             return photo.getImage().startsWith("photos-presse/"+theme);
         }
     }
     
     private Photos() throws IOException {
         ParimagineConfig config = ConfigRegistry.getConfig(ParimagineConfig.class);
         if (config != null) {
             documents = config.getDocuments();
             verify    = config.getVerify();
         } else {
             documents = null;
             verify    = false;
         }
         
         list = load();
         image2photoMap = new HashMap<>();
         for (Photo photo : list) {
             image2photoMap.put(photo.getImage(), photo);
         }
         createIndex();
         Collections.sort(list);
         for (int i = 1; i <= 20; i++) {
             List<Photo> districtList = newArrayList(filter(list, new PredicateDistrict(i)));
             districtLists.put(i, districtList );
         }
         for (String theme :themes) {
             List<Photo> themeList = newArrayList(filter(list, new PredicateTheme(theme)));
             themeLists.put(theme, themeList );
         }
     }
     
     public int getSize() {
         return this.list.size();
     }
 
     public List<Photo> getSlice( int page, int count ) {
     	return verify(new Slice(this.list, page, count).getPhotos());
     }
 
     public List<Photo> getDistrictSlice(Integer district, int page, int count) {
     	
         if (district == null || district == 0) {
             return getSlice(page, count);
         }
         
         if (20 < district) {
             throw new IllegalArgumentException("Il n'y a que 20 arrondissements à Paris. Tu demandes le N° "+district);
         }
         
         List<Photo> districtList = districtLists.get(district);
         
         return verify(new Slice(districtList, page, count).getPhotos());
     }
     
     public List<Photo> getThemeSlice(String theme, int page, int count) {
 
     	if (theme == null || theme.length() == 0)  {
             return getSlice(page, count);
         }
     	
         int iTheme = Arrays.binarySearch(themes, theme);
         
         if (iTheme == -1) {
             throw new IllegalArgumentException("Je n'ai pas trouvé ce thème dans la liste. Tu demandes le thème '"+theme+"'. Voici la liste : " + themes);
         }
         
         List<Photo> themeList = themeLists.get(theme);
         
         return verify(new Slice(themeList, page, count).getPhotos());
     }
     
 	public List<Photo> getRandomSlice(int page, int count) {
         return verify(new Slice(this.list, page, count).getRandomPhotos());
 	}
 	
     public List<Photo> search(String searchString, int maxResults) throws IOException, ParseException {
         if (searchString == null || searchString.length()==0) {
             throw new IllegalArgumentException();
         }
         if (maxResults<0) {
             return Collections.emptyList();
         }
         List<Photo> list = new ArrayList<>(maxResults);
         
         IndexReader reader = DirectoryReader.open(index);
         IndexSearcher searcher = new IndexSearcher(reader);
         QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_43, new String[] { "didascalie.base", "didascalie.ext", "street", "legacy" }, analyzer);
         Query query = parser.parse(searchString);
         TopDocs results = searcher.search(query, maxResults);
         ScoreDoc[] hits = results.scoreDocs;
         int countResults = 0;
         for (ScoreDoc scoreDoc : hits) {
             Document doc = searcher.doc(scoreDoc.doc);
             String image = doc.get("image");
             list.add(image2photoMap.get(image));
             if (++countResults>=maxResults) {
                 break;
             }
         }
         reader.close();
         return verify(list);
     }
 
     private ArrayList<Photo> load() throws IOException {
         ArrayList<Photo> fromJSON;
         ObjectMapper mapper = new ObjectMapper();
         
         URL json = Photos.class.getResource("/photos-test.json");
         if (json == null) {
             json = Photos.class.getResource("/photos.json");
         }
         if (json == null) {
             throw new IOException("Not found! Neither /photos-test.json nor /photos.json on the classpath of the "+Photos.class.getName()+" classloader.");
         }
         
         try (InputStream jsonSource = json.openStream()) {
             fromJSON = mapper.readValue(jsonSource, new TypeReference<ArrayList<Photo>>() { } );
         }
         int index = 0;
         for (Photo photo : fromJSON) {
             photo.setIndex(index++);
         }
         return fromJSON;
     }
     
     private List<Photo> verify(List<Photo> list) {
         if (!verify) {
             return list;
         }
         if (documents == null || documents.length() == 0) {
             return list;
         }
         for (Photo photo : list) {
             if (photo.getReachable()) {
                 continue;
             }
 
             URL url = null;
             try {
                 url = new URL(toURL(null, photo.getImage()));
                 if (url != null) {
                     HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                     try (InputStream is = conn.getInputStream()) {
                         if (is != null) {
                             photo.setReachable(true);
                             continue;
                         }
                     }
                 }
             } catch (Exception e) {
             } finally {
                 if (photo.getReachable()) {
                     log.info("FOUND {} @ {}", photo.getImage(), url.toString());
                 } else {
                     log.info("NOT FOUND {}", photo.getImage());
                 }
             }
         }
         return list;
     }
     
     public static void writePhotoList(Writer writer, List<Photo> photoList) throws IOException {
         ObjectMapper mapper = new ObjectMapper();
         DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
 
         Collections.sort(photoList, Photo.comparator);
         
         mapper.writer(prettyPrinter).writeValue(writer, photoList);
     }
 
     Directory index = new RAMDirectory();
     Analyzer analyzer = new FrenchAnalyzer(Version.LUCENE_43);
     
     private void createIndex() throws IOException {
         IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer);
     
         iwc.setOpenMode(OpenMode.CREATE);
     
         IndexWriter writer = new IndexWriter(index, iwc);
         for (Photo photo : list) {
             // make a new, empty document
             Document doc = new Document();
     
             Field image = new StringField("image", photo.getImage(), Field.Store.YES);
             doc.add(image);
             doc.add(new TextField("didascalie.base", photo.getDidascalie().getBase(), Field.Store.NO));
             doc.add(new TextField("didascalie.ext", photo.getDidascalie().getExt(), Field.Store.NO));
             doc.add(new TextField("street", photo.getAddress().getStreet(), Field.Store.NO));
             doc.add(new TextField("legacy", photo.getAddress().getLegacy(), Field.Store.NO));
     
             writer.addDocument(doc);
         }
         writer.close();
     }
 
 	public Photo getPhoto(String image) {
 		return image2photoMap.get(image);
 	}
 
 	public Photo getPhoto(int index) {
 		return list.get(index);
 	}
 
 	public List<Photo> getPhotos() {
 		return list;
 	}
 
 	private static class Slice {
 		
         final List<Photo>  set;
 	    final int          from;
 	    final int          to;
 	    
         public Slice(final List<Photo> photos, final Integer offset, final Integer size) {
             if (photos == null) {
                 throw new IllegalArgumentException();
             }
 
             if (offset == null) {
                 throw new IllegalArgumentException();
             }
 
             if (size == null) {
                 throw new IllegalArgumentException();
             }
 
             this.set    = photos;
 	    	this.from = Math.min(size*offset, this.set.size());	
 	    	this.to   = Math.min(size*(offset+1), this.set.size());
 		}
         
         List<Photo> getPhotos() {
             if (from == to) {
                 return Collections.emptyList();
             }
             return this.set.subList(this.from, this.to);
         }
 
         List<Photo> getRandomPhotos() {
             if (from == to) {
                 return Collections.emptyList();
             }
             List<Photo> randomSlice = new ArrayList<>(this.to);
             
             Random r = new Random(new Date().getTime());
             for (int i = 0; i < this.to; i++) {
                 randomSlice.add(this.set.get(r.nextInt(this.set.size())));
             }
             return randomSlice;
         }
 	}
 	
 	public String toURL(HttpServletRequest request, String path) {
 	    final String ret;
 	    if (documents == null || documents.length() == 0) {
 	        if (request != null) {
 	            ret = RequestUtils.getBaseURL(request) + "/documents";
 	        } else {
 	            return null;
 	        }
 	    } else {
 	        ret = documents; 
 	    }
 	    if (ret != null) {
 	        if (path != null) {
	            if (ret.endsWith("/")) {
                    return ret + path;
	            } else {
                    return ret + "/" + path;
	            }
 	        }
 	    }
 	    return ret; 
 	}
 }
