 package prixma.csv.example;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.csvreader.CsvReader;
 
 import prixma.csv.datamapper.CsvDataConverter;
 import prixma.csv.exceptions.CsvConvertException;
 
 public class ClientConverter implements CsvDataConverter<Client> {
 
 	@Override
 	public Client convertRow(CsvReader reader) {
 		
 		Client client = new Client();
 		
 		try {
 			
 			client.setName(reader.get("Name"));
 			client.setAge(Integer.parseInt(reader.get("Age")));
 		
 		} catch (IOException e) {
 			throw new CsvConvertException(e);
 		}
 		
 		return client;
 		
 	}
 
 	@Override
 	public boolean canHandle(List<String> headers) {
		return headers.containsAll(Arrays.asList("Name", "Age"));
 	}
 
 }
