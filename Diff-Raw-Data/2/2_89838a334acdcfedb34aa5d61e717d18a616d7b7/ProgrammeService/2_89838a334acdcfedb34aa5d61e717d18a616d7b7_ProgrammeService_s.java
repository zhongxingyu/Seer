 package fr.ybo.services;
 
 import com.couchbase.client.CouchbaseClient;
 import com.couchbase.client.protocol.views.ComplexKey;
 import com.couchbase.client.protocol.views.Query;
 import com.couchbase.client.protocol.views.View;
 import com.couchbase.client.protocol.views.ViewRow;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import fr.ybo.modele.Programme;
 
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 import java.util.*;
 
 public class ProgrammeService extends DataService<Programme> {
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Programme> getAll() throws ServiceExeption {
         throw new RuntimeException("Programme.getAll is not accessible");
     }
 
     @Override
     public Programme getById(String id) throws ServiceExeption {
         throw new RuntimeException("Programme.getId is not accessible");
     }
 
     private List<Programme> getByChannel(String channel) throws ServiceExeption {
         try {
             CouchbaseClient client = CouchBaseService.INSTANCE.getClient();
             ObjectMapper mapper = CouchBaseService.INSTANCE.getMapper();
 
             View view = client.getView("programme", "by_channel");
             Query query = new Query();
            query.setKey(channel);
             query.setIncludeDocs(true);
 
             List<Programme> programmes = new ArrayList<Programme>();
             for (ViewRow row : client.query(view, query)) {
                 programmes.add(mapper.readValue((String) row.getDocument(), Programme.class));
             }
 
             return programmes;
         } catch (IOException e) {
             throw new ServiceExeption(e);
         }
     }
 
     @Override
     public List<Programme> getBy(String parameterName, String parameterValue) throws ServiceExeption {
         if ("id".equals(parameterName)) {
             return Collections.singletonList(getById(parameterValue));
         } else if ("channel".equals(parameterName)) {
             return getByChannel(parameterValue);
         }
         return null;
     }
 
     private Collection<Programme> getByChannelAndDate(String channel, String date) throws ServiceExeption {
 
         try {
             CouchbaseClient client = CouchBaseService.INSTANCE.getClient();
             ObjectMapper mapper = CouchBaseService.INSTANCE.getMapper();
 
             View view = client.getView("programme", "by_date");
             Query query = new Query();
 
             query.setRange(ComplexKey.of("00000000000000", date),
                     ComplexKey.of(date, "99999999999999"));
             query.setIncludeDocs(true);
 
             List<Programme> programmes = new ArrayList<Programme>();
             for (ViewRow row : client.query(view, query)) {
                 Programme programme = mapper.readValue((String) row.getDocument(), Programme.class);
                 if (programme.getChannel().equals(channel)) {
                     programmes.add(programme);
                 }
             }
             return programmes;
         } catch (IOException ioException) {
             throw new ServiceExeption(ioException);
         }
     }
 
     private List<Programme> getByChannelAndBetweenDate(String channel, final String dateDebut, final String dateFin) throws ServiceExeption {
         try {
             CouchbaseClient client = CouchBaseService.INSTANCE.getClient();
             ObjectMapper mapper = CouchBaseService.INSTANCE.getMapper();
             Map<String, Programme> programmes = new HashMap<String, Programme>();
 
             View view = client.getView("programme", "by_date");
             Query query = new Query();
 
             query.setRange(ComplexKey.of(dateDebut, "00000000000000"),
                     ComplexKey.of(dateFin, "99999999999999"));
             query.setIncludeDocs(true);
 
             for (ViewRow row : client.query(view, query)) {
                 Programme programme = mapper.readValue((String) row.getDocument(), Programme.class);
                 if (programme.getChannel().equals(channel)) {
                     programmes.put(programme.getId(), programme);
                 }
             }
 
             query = new Query();
             query.setRange(ComplexKey.of("00000000000000", dateDebut),
                     ComplexKey.of("99999999999999", dateFin));
             query.setIncludeDocs(true);
 
             for (ViewRow row : client.query(view, query)) {
                 Programme programme = mapper.readValue((String) row.getDocument(), Programme.class);
                 if (programme.getChannel().equals(channel)) {
                     programmes.put(programme.getId(), programme);
                 }
             }
 
             List<Programme> programmeList = new ArrayList<Programme>(programmes.values());
 
             Collections.sort(programmeList, new Comparator<Programme>() {
                 @Override
                 public int compare(Programme programme, Programme programme1) {
                     return programme.getStart().compareTo(programme1.getStart());
                 }
             });
             return programmeList;
         } catch (IOException ioException) {
             throw new ServiceExeption(ioException);
         }
 
     }
 
 
     @Override
     public List<Programme> get(String... parameters) throws ServiceExeption {
         if (parameters.length == 4) {
             if ("channel".equals(parameters[0])
                     && "date".equals(parameters[2])) {
                 return new ArrayList<Programme>(getByChannelAndDate(parameters[1], parameters[3]));
 
             }
         } else if (parameters.length == 6) {
             if ("channel".equals(parameters[0])
                     && "datedebut".equals(parameters[2])
                     && "datefin".equals(parameters[4])) {
                 return new ArrayList<Programme>(getByChannelAndBetweenDate(parameters[1], parameters[3], parameters[5]));
             }
         }
         return null;
     }
 
 
 }
