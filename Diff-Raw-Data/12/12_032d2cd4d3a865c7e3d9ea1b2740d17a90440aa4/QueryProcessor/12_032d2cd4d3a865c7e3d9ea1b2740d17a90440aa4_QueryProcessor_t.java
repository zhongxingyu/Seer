 package com.develogical;
 
 public class QueryProcessor {
 
     public String process(String query) {
         if (query.contains("programming")) {
             return "Computer programming is the comprehensive process that leads from an original " 
                    + "formulation of a computing problem to executable programs.";
         } else if (query.contains("cakes")) {
             return "Battenberg cake is a light sponge cake. The cake is covered in marzipan and, " +
                     "when cut in cross section, displays a distinctive two-by-two check pattern " +
                     "alternately coloured pink and yellow.";
         } else if (query.contains("cheese")) {
             return "Cheese is a food derived from milk that is produced in a wide range of flavors, " +
                     "textures, and forms by coagulation of the milk protein casein. " +
                     "It comprises proteins and fat from milk, usually the milk of " +
                     "cows, buffalo, goats, or sheep. During production, the milk is " +
                     "usually acidified, and adding the enzyme rennet causes coagulation. " +
                     "The solids are separated and pressed into final form.";
        } else if (query.contains("chocolate")) {
            return "Chocolate is a processed, typically sweetened food produced " +
                    "from the seed of the tropical Theobroma cacao tree. Cacao " +
                    "has been cultivated for at least three millennia in " +
                    "Mexico, Central America and Northern South America.";
         }
         return "";
     }
 }
