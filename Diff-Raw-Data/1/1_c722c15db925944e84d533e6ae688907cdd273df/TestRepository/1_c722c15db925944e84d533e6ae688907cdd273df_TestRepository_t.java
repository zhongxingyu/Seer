 package fuschia.tagger.common;
 
 public class TestRepository {
 
 	public static void main(String[] args) {
 		DocumentRepository repo = DocumentRepository.get911();
 		System.out.println(repo.getAll().size());
 	}
 
 }
