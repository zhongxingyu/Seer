 package eu.margiel.domain;
 
 import static org.fest.assertions.Assertions.*;
 
 import java.util.List;
 
 import org.junit.Test;
 
 public class SponsorTypeShould {
 	@Test
 	public void getAllShortNames() {
 		List<String> shortNames = SponsorType.allShortNames();
 
		assertThat(shortNames).containsExactly("Złoty", "Srebrny", "Brązowy");
 	}
 }
