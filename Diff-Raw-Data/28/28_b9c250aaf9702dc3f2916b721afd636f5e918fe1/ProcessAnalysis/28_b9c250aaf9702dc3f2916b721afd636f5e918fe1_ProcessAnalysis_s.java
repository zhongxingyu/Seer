 package process_analysis_core;
 
 import analyze.impl.AnalysisInversionMethod;
 import analyze.impl.AnalysisSeriesMethod;
 import interfaces.IArrayGenerator;
 import regression_generator.impl.RegressionArrayGeneratorModel1;
 import regression_generator.help.DistributionCalcHelper;
 import regression_generator.impl.RegressionArrayGeneratorModel2;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mark
  * Date: 15.03.13
  * Time: 19:45
  * To change this template use File | Settings | File Templates.
  */
 public class ProcessAnalysis {
     public static void main(String[] args) {
         int N = 80;
         int processArrayLength = 5000;
         int parts = 100;
         double alpha1 = 0.9;
         double alpha2 = -0.6;
         double z1 = -0.2;
         double z2 = 0.5;
 
         IArrayGenerator generator = new RegressionArrayGeneratorModel1(alpha1,alpha2,z1,z2);
         AnalysisInversionMethod inversion  = new AnalysisInversionMethod();
         AnalysisSeriesMethod  series = new AnalysisSeriesMethod();
 
          //Генерируем N значений и для них считаем оценки методом инверсий и серий
         System.out.println("\nЗадание2:");
         double[] smallExperementalArray = generator.generateSeries(N);
         System.out.println("SmallExperementalArray("+N+")\n\tSeries: "
                         +series.calcEstimate(smallExperementalArray)+ "\n\tInversion: "
                         +inversion.calcEstimate(smallExperementalArray));
 

         System.out.println("\nЗадание 3:");
        processAnalysis(generator,processArrayLength,parts,inversion,series);
         System.out.println("Задание 4:");
         IArrayGenerator generator2 = new RegressionArrayGeneratorModel2(alpha1,alpha2,z1,z2){
             //Функция из модели 2
             @Override
             protected double additionalFunc(int i) {
                 return (i - i*i / 2000.0)/1000.0;
             }
         };
        processAnalysis(generator2,processArrayLength,parts,inversion,series);
     }
     private static void processAnalysis(IArrayGenerator generator,
                                         int processArrayLength,
                                        int parts,
                                        AnalysisInversionMethod inversion,
                                        AnalysisSeriesMethod series){
         //Генерим  processArrayLength значений
         double[] experementalData = generator.generateSeries(processArrayLength);
 
         //Разбиваем на части
         ArrayList<double[]> splitted = splitByParts(experementalData,parts);
 
         //Считаем  мат ожидания и отклонения для каждой части,
         // сохраняем в массив
         double[] mxArray = new double[splitted.size()];
         double[] sigmaArray = new double[splitted.size()];
         fillMxAndSigmaArrays(splitted,mxArray,sigmaArray);
 
         //Расчитываем по две оценки для каждого массива
         System.out.println("MX\n\tInversion: "
                 + inversion.calcEstimate(mxArray)
                 + "\n\tSeries: "+series.calcEstimate(mxArray));
         System.out.println("Sigma\n\tInversion: "
                 + inversion.calcEstimate(sigmaArray)
                 + "\n\tSeries: "+series.calcEstimate(sigmaArray));
     }
     private  static void fillMxAndSigmaArrays(ArrayList<double[]> splitted,double[] mxArray,double[] sigmaArray){
         for(int i=0;i<splitted.size();i++){
             //Для каждой из частей массива
             // расчитываем мат ожидание
             // и среднее отклонение
             double[] partArray = splitted.get(i);
             mxArray[i] = DistributionCalcHelper.calcMx(partArray);
             sigmaArray[i] = DistributionCalcHelper.calcSigma(partArray);
         }
     }
     //Разбивает массив original на части размером parts
     private  static ArrayList<double[]> splitByParts(double[] original,int parts){
         if(original.length % parts !=0){
             throw new IllegalArgumentException("невозможно разделить массив на равные части(не делится нацело)");
         }
         ArrayList<double[]> result = new ArrayList<double[]>();
         for(int i=0;i<original.length;i = i + parts){
         //Копируем в tempArray начиная с позиции 0
         //массив original начиная с индекса i
         //длинной parts
          double[] tempArray = new double[parts];
          System.arraycopy(original, i, tempArray, 0, parts);
          result.add(tempArray);
         }
         return result;
     }
 }
