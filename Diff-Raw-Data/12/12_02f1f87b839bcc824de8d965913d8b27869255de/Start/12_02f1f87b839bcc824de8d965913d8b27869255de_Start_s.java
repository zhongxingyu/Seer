 package main;
 
 import main.core.Protein;
 import main.core.Structure;
 import main.estimation.MatthewsCorrelationCoefficient;
 import main.estimation.Q3;
 import main.prediction.Gor3;
 import main.statistics.AminoAcidStatistics;
 import main.statistics.ChouFasman;
 import main.tools.Loader;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  */
 public class Start {
 
     public static void main(String[] args) {
         Loader loader = new Loader("dssp_info.csv");
         List<Protein> proteinListDssp = loader.readFile();
         loader = new Loader("stride_info.csv");
         List<Protein> proteinListStride = loader.readFile();
 
         System.out.println("DSSP amino acid frequencies:");
         System.out.println(AminoAcidStatistics.getAminoAcidNameSortStats(proteinListDssp));
 
         System.out.println("STRIDE amino acid frequencies:");
         System.out.println(AminoAcidStatistics.getAminoAcidNameSortStats(proteinListStride));
 
         ChouFasman chouFasmanDssp = new ChouFasman(proteinListDssp);
         System.out.println("DSSP amino acids propensities:");
         System.out.println(chouFasmanDssp.getAminoAcidsPropensitiesString());
 
         ChouFasman chouFasmanStride = new ChouFasman(proteinListStride);
         System.out.println("STRIDE amino acids propensities:");
         System.out.println(chouFasmanStride.getAminoAcidsPropensitiesString());
 
         System.out.println("            DSSP           STRIDE");
 
         double dsspSum = 0;
         double strideSum = 0;
 
         for (int i = 0; i < proteinListDssp.size(); i++) {
             Protein proteinDssp = proteinListDssp.remove(0);
             chouFasmanDssp = new ChouFasman(proteinListDssp);
             Gor3 gor3Dssp = new Gor3(chouFasmanDssp);
             Protein predictedProteinDssp = gor3Dssp.makePrediction(proteinDssp);
 
             Protein proteinStride = proteinListStride.remove(0);
             chouFasmanStride = new ChouFasman(proteinListStride);
             Gor3 gor3Stride = new Gor3(chouFasmanStride);
             Protein predictedProteinStride = gor3Stride.makePrediction(proteinStride);
 
 //            for(Structure structure : proteinDssp.getProteinStructure()){
 //                if(structure!=null)System.out.print(structure.toString().substring(0, 1));else
 //                    System.out.print(" ");
 //            }
 //            System.out.println();
 //            for(Structure structure : predictedProteinDssp.getProteinStructure()){
 //                if(structure!=null)System.out.print(structure.toString().substring(0, 1));
 //            }
 //            System.out.println();
 
             System.out.println(proteinDssp.getPdbCode() + "_" + proteinDssp.getPdbChainCode());
             System.out.println(String.format("Q3:         %1$.2f%%         %2$.2f%%", Q3.getQ3Quality(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure()) * 100, Q3.getQ3Quality(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure()) * 100));
             dsspSum += Q3.getQ3Quality(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure()) * 100;
             strideSum += Q3.getQ3Quality(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure()) * 100;
            String mccHelixDssp = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.HELIX) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.HELIX));
            String mccBetaDssp = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.BETA) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.BETA));
            String mccCoilDssp = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.COIL) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinDssp.getProteinStructure(), predictedProteinDssp.getProteinStructure(), Structure.COIL));
            String mccHelixStride = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.HELIX) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.HELIX));
            String mccBetaStride = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.BETA) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.BETA));
            String mccCoilStride = (MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.COIL) < 0) ? "error" : String.format("%1$.3f", MatthewsCorrelationCoefficient.getMatthewsCorrelationCoefficient(proteinStride.getProteinStructure(), predictedProteinStride.getProteinStructure(), Structure.COIL));
             System.out.println(String.format("MCC HELIX:  %1$s          %2$s", mccHelixDssp, mccHelixStride));
             System.out.println(String.format("MCC BETA:   %1$s          %2$s", mccBetaDssp, mccBetaStride));
             System.out.println(String.format("MCC COIL:   %1$s          %2$s", mccCoilDssp, mccCoilStride));
             proteinListDssp.add(proteinDssp);
             proteinListStride.add(proteinStride);
         }
 
         System.out.println(String.format("DSSP average: %1$.3f%%", dsspSum / proteinListDssp.size()));
         System.out.println(String.format("STRIDE average: %1$.3f%%", strideSum / proteinListStride.size()));
 
     }
 
 }
