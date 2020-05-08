 import acme.telecom.fakes.FakeTariff;
 import fit.ColumnFixture;
 import fit.Parse;
 
 import java.math.BigDecimal;
 
 public class GivenTheFollowingTariffs extends ColumnFixture {
     public String Name;
     public String PeakRate;
     public String OffPeakRate;
 
     @Override
     public void doRows(Parse rows) {
         SystemUnderTest.tariffLibrary.reset();
         super.doRows(rows);
     }
 
     @Override
     public void reset() throws Exception {
         Name = null;
         PeakRate = null;
         OffPeakRate = null;
     }
 
     @Override
     public void execute() throws Exception {
         BigDecimal peakRate = BigDecimal.valueOf(Double.parseDouble(this.PeakRate) / 60);
        BigDecimal offPeakRate = BigDecimal.valueOf(Double.parseDouble(this.PeakRate) / 60);
         SystemUnderTest.tariffLibrary.addTariff(this.Name, new FakeTariff(peakRate, offPeakRate));
     }
 }
