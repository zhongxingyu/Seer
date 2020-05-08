 package Project.NLP.Tests;
 
 import Project.NLP.Pipeline;
 import edu.stanford.nlp.ling.TaggedWord;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  *
  */
 public class PipelineTest {
 
     Pipeline pipeline;
 
 
     @Before
     public void setUp() {
         pipeline = new Pipeline();
     }
 
     @Test
     public void testStripSender() {
         String[] result = pipeline.extractSenderAndMessage("Blue: peanut");
         assertEquals("Blue", result[0]);
         assertEquals("peanut", result[1]);
     }
 
     @Test
    public void testGetFirstInstance(){
        ArrayList<TaggedWord> words = new ArrayList<>();
        words.add(new TaggedWord("Build", "VB"));
        words.add(new TaggedWord("a", "DT"));
        words.add(new TaggedWord("base", "NN"));
 
         List<TaggedWord> result = pipeline.getFirstInstance(words, "VB", "DT", "NN");
         assertNotNull(result);
         assertEquals(3, result.size());
 
     }
 }
