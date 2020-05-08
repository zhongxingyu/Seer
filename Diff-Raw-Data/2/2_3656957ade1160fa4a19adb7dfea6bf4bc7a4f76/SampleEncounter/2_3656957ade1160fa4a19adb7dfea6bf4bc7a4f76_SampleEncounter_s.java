 package org.bahmni.feed.openerp.testhelper;
 
 import org.bahmni.feed.openerp.ObjectMapperRepository;
 import org.bahmni.feed.openerp.OpenMRSEncounterParser;
 import org.bahmni.feed.openerp.domain.encounter.OpenMRSEncounter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Scanner;
 
 public class SampleEncounter {
 
     public static String json() {
         InputStream resourceAsStream = SampleEncounter.class.getClassLoader().getResourceAsStream("encounterResource.json");
         return new Scanner(resourceAsStream).useDelimiter("\\Z").next();
     }
 
     public static OpenMRSEncounter encounter() throws IOException {
         return new OpenMRSEncounterParser(ObjectMapperRepository.objectMapper).parse(SampleEncounter.json());
     }
 
     public static String requestParams(){
        return "{\"id\":\"f18b7270-0085-4196-a6e9-367ebcbfc890\",\"openERPOrders\":[{\"id\":\"26141644-468c-4e71-8d1c-8c17fd3b6df0\",\"productIds\":[\"4a3ad265-34f7-4fbb-9cd5-d52289afea7a\"],\"visitId\":\"294f02b4-78d4-4b3a-831b-40b667efe3b8\",\"type\":null,\"startDate\":null,\"description\":\"null 29/05/2013 05:59:23\"},{\"id\":\"3f9960ef-f141-4131-a4b5-23835d69671d\",\"productIds\":[\"2461e8c0-ef23-475f-934d-b8d8366d5e90\"],\"visitId\":\"294f02b4-78d4-4b3a-831b-40b667efe3b8\",\"type\":null,\"startDate\":null,\"description\":\"null 29/05/2013 05:59:23\"},{\"id\":\"f81749c4-4827-4374-be64-28e5f52513f5\",\"productIds\":[\"2461e8c0-ef23-475f-934d-b8d8366d5e90\"],\"visitId\":\"294f02b4-78d4-4b3a-831b-40b667efe3b8\",\"type\":null,\"startDate\":null,\"description\":\"null 29/05/2013 05:59:23\"}]}";
     }
 
 }
