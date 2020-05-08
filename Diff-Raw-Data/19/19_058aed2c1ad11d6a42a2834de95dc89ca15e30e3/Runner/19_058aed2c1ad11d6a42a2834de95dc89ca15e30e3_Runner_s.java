 import patent.Claims;
 import patent.Family;
import process.Output;
 import process.PreProcess;
 import process.Summary;
 
 public class Runner {
 
    public static void main(String[] args)  {
 
         Claims claims = new Claims();
         PreProcess preProcess = new PreProcess();
         Family family = new Family();
        Output output = new Output();
 
         String text = preProcess.read("src/main/resources/file.xml");
        output.writePriorityClaims(claims.extractClaims(text));
         Summary summary = new Summary();
         summary = family.extract(text);
        output.WriteSummary(summary);
     }
 }
