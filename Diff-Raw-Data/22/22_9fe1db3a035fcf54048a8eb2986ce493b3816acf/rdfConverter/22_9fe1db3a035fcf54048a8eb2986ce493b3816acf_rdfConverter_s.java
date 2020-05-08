 /* MASWS Assignment 2
 Diljá Rudolfsdóttir & Steven Eardley 2013
 */
 
 import java.io.*;
 import java.util.*;
 
 public class rdfConverter{
 
 public static void main(String args[]) {
     try{
 
         FileInputStream fstream = new FileInputStream(args[0]);
         
         BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
         String strLine;
         
         FileWriter fstream3 = new FileWriter("RDF-Output.ttl", false);
         BufferedWriter out = new BufferedWriter(fstream3);
         
         out.write("@prefix dbpedia: <http://dbpedia.org/resource/> .\n");
         out.write("@prefix vso: <http://www.heppnetz.de/ontologies/vso/ns#> .\n");
         out.write("@prefix gr:  <http://purl.org/goodrelations/v1#> .\n");
         out.write("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
         out.write("@prefix mot: <http://vocab.inf.ed.ac.uk/mot/> .\n\n");
 
         while (((strLine = br.readLine()) != null)) {
             String [] objects = strLine.split("\\|");
             
             // Create a class for the Test
             out.write(String.format("_:%s a mot:Test ;\n", objects[0]));
 
             // Add test properties
             out.write(String.format("\tmot:testid \"%s\"^^xsd:integer ;\n", objects[0]));
             
             out.write(String.format("\tmot:testvehicle _:%s ;\n", objects[1]));
 
             out.write(String.format("\tmot:testdate \"%s\"^^xsd:date ;\n", objects[2]));
             
             out.write(String.format("\tmot:testclassid \"%s\"^^xsd:string ;\n", objects[3]));
             
             out.write(String.format("\tmot:testtype \"%s\"^^xsd:string ;\n", objects[4]));
 
             out.write(String.format("\tmot:testresult \"%s\"^^xsd:string ;\n", objects[5]));
                              
             out.write(String.format("\tmot:testlocation dbpedia:%s_postcode_area .\n", objects[7]));
              
             // Create a class for the vehicle
             out.write(String.format("_:%s a mot:Vehicle ;\n", objects[1]));
             
             out.write(String.format("\tvso:mileageFromOdometer [ a gr:QuantitativeValueFloat ;\n\t\tgr:hasValueFloat \"%s\"^^xsd:float ;\n\t\tgr:hasUnitOfMeasurement \"SMI\"^^xsd:string] ;\n", objects[6]));
             
             out.write(String.format("\tmot:vehicleid \"%s\"^^xsd:integer ;\n", objects[1]));
             
            out.write(String.format("\tgr:hasManufacturer dbpedia:%s ;\n", (objects[8].charAt(0)) + objects[8].substring(1).toLowerCase()));
 
             out.write(String.format("\tmot:vehiclemodel \"%s\"^^xsd:string ;\n", objects[9]));
             
             out.write(String.format("\tvso:color \"%s\" ;\n", objects[10]));
 
             String fuelType;
             char sw = objects[11].charAt(0);
             switch (sw) {
                 case 'P':  fuelType = "dbpedia:Gasoline";
                         break;
                 case 'D':  fuelType = "dbpedia:Diesel_fuel";
                         break;
                 case 'E':  fuelType = "dbpedia:Electricity";
                         break;
                 case 'S':  fuelType = "dbpedia:Steam";
                         break;
                 case 'N':  fuelType = "dbpedia:Liquefied_natural_gas";
                         break;                        
                 case 'F':  fuelType = "dbpedia:Fuel_cell";
                         break;
                 case 'C':  fuelType = "dbpedia:Compressed_natural_gas";
                         break;
                 case 'L':  fuelType = "dbpedia:Liquefied_petroleum_gas";
                         break;
                case 'O':  fuelType = "Other";
                         break;                        
                 default: fuelType = "None Found";
                         break;
             }
             out.write(String.format("\tvso:fuelType %s ;\n", fuelType));
 
             out.write(String.format("\tvso:engineDisplacement [ a gr:QuantitativeValueFloat ;\n\t\tgr:hasValueFloat \"%s\"^^xsd:float ;\n\t\tgr:hasUnitOfMeasurement \"CMQ\"^^xsd:string ] ;\n", objects[12]));
                              
             out.write(String.format("\tvso:firstRegistration \"%s\"^^xsd:date .\n\n", objects[13]));
         }
         System.out.println("\nSuccess! Written to RDF-Output.ttl\n");
         out.close();
         fstream.close();
     }
     
     catch (Exception e) {// Catch exception if any
     System.err.println("Error: " + e.getMessage());
     }
     }
 }
