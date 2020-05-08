import outWriter.implementation.WriteClaims;
import outWriter.implementation.WriteSummary;
 import patent.Claims;
 import patent.Family;
 import process.PreProcess;
 import process.Summary;
 
 public class Runner {
 
    public static void main(String[] args) {
 
         Claims claims = new Claims();
         PreProcess preProcess = new PreProcess();
         Family family = new Family();
 
         String text = preProcess.read("src/main/resources/file.xml");

        WriteClaims writeClaims = new WriteClaims();
        writeClaims.write(claims.extractClaims(text), "src/main/resources/outputClaims.xml");
         Summary summary = new Summary();
         summary = family.extract(text);
        WriteSummary writeSummary = new WriteSummary();
        writeSummary.write(summary, "src/main/resources/outputSummary.xml");

     }
 }
