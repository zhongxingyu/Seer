 package greed.model;
 
 import com.topcoder.shared.problem.DataType;
 import com.topcoder.shared.problem.TestCase;
 import greed.code.LanguageManager;
 import greed.code.LanguageTrait;
 
 /**
  * Greed is good! Cheers!
  */
 public class Convert {
     public static Contest convertContest(com.topcoder.client.contestant.ProblemComponentModel problem) {
         String fullName = problem.getProblem().getRound().getContestName();
         boolean hasDivision = fullName.contains("DIV");
         Integer div = null;
         String contestName;
         if (! hasDivision) {
             contestName = fullName;
         } else {
             int sp = fullName.indexOf("DIV");
             contestName = fullName.substring(0, sp - 1);
             String divNum = fullName.substring(sp + 4);
             div = Integer.parseInt(divNum);
         }
         if (contestName.contains("/") ) {
             contestName = contestName.replace("/", "-");
         }
         
         return new Contest(contestName, div);
     }
 
     public static Language convertLanguage(com.topcoder.shared.language.Language lang) {
         String langName = lang.getName();
         if ("C++".equals(langName)) return Language.CPP;
         if ("Python".equals(langName)) return Language.PYTHON;
         if ("Java".equals(langName)) return Language.JAVA;
         if ("C#".equals(langName)) return Language.CSHARP;
        if ("VB".equals(langName)) return Language.VB; // Unsupported
         return null;
     }
     
     private static String getRidOfTopElement(String xml)
     {
         // Unfortunately, since the parameter is not guaranteed to be strictly
         //  correct XML, we cannot rely on the usual XML parser. 
         xml = xml.trim();
         int i = 0, j = xml.length() - 1;
         if (    (xml.length() >= 4)
              && (xml.charAt(i) == '<')
              && (xml.charAt(j) == '>')
            ) {
             i++; j--;
             while ( (i < xml.length()) && (xml.charAt(i) != '>') ) {
                 i++;
             }
             while ( (j >= 0) && (xml.charAt(j) != '<') ) {
                 j--;
             }
             if (i < j) {
                 xml = xml.substring(i + 1, j);
             }
             xml = xml.trim();
         }
         return xml;
     }
 
     public static Problem convertProblem(com.topcoder.client.contestant.ProblemComponentModel problem, Language language) {
         Param[] params = new Param[problem.getParamNames().length];
         for (int i = 0; i < params.length; ++i)
             params[i] = new Param(problem.getParamNames()[i], convertType(problem.getParamTypes()[i]), i);
         Method method = new Method(problem.getMethodName(), convertType(problem.getReturnType()), params);
 
         LanguageTrait trait = LanguageManager.getInstance().getTrait(language);
         Testcase[] cases = new Testcase[problem.getTestCases().length];
         for (int i = 0; i < cases.length; ++i) {
             TestCase tc = problem.getTestCases()[i];
             ParamValue[] input = new ParamValue[tc.getInput().length];
             for (int j = 0; j < input.length; j++)
                 input[j] = trait.parseValue(tc.getInput()[j], params[j]);
             ParamValue output = trait.parseValue(tc.getOutput(), new Param("expected", method.getReturnType(), params.length));
             cases[i] = new Testcase(i, input, output);
 
             if (tc.getAnnotation() != null) {
                 String ann = getRidOfTopElement(tc.getAnnotation().toXML());
                 if ( ann.length() != 0 ) {
                     cases[i].setAnnotation(ann);
                 }
             }
         }
 
         String[] notes = new String[problem.getNotes().length];
         for (int i = 0; i < notes.length; ++i)
             notes[i] = problem.getNotes()[i].toXML();
         String[] constraints = new String[problem.getConstraints().length];
         for (int i = 0; i < constraints.length; ++i)
             constraints[i] = problem.getConstraints()[i].toXML();
 
         return new Problem(
                 problem.getProblem().getName(),
                 problem.getPoints().intValue(),
                 problem.getClassName(),
                 method,
                 cases,
                 new ProblemDescription(
                         problem.getIntro().toXML(),
                         notes,
                         constraints
                 )
         );
     }
 
     public static Type convertType(DataType dt) {
         String typeName = dt.getBaseName();
         Primitive type = null;
         if ("int".equals(typeName)) type = Primitive.INT;
         if ("String".equals(typeName)) type = Primitive.STRING;
         if ("long".equals(typeName)) type = Primitive.LONG;
         if ("double".equals(typeName)) type = Primitive.DOUBLE;
         if ("bool".equals(typeName)) type = Primitive.BOOL;
         return new Type(type, dt.getDimension());
     }
 }
