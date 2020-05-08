 package ru.kirillova.search.database;
 
 import ru.chuprikov.search.gather.ProblemRawData;
 
 class UvaContentParser implements ContentParser {
         private String getTextBody(String s, boolean flag, int skip) {
             StringBuilder str = new StringBuilder();
             for (int i = 0; i < s.length(); ++i) {
                 if (s.charAt(i) == '<') {
                     if (s.charAt(i + 1) == '/') {
                         if (skip > 0) {
                             --skip;
                         }
                         if (!flag) {
                             return str.toString();
                         }
                     } else if (!Character.isLetter(s.charAt(i + 1))) {
                         if (skip == 0) {
                             str.append(s.charAt(i));
                         }
                         continue;
                     }
                     for (; s.charAt(i) != '>'; ++i)
                         ;
                     continue;
                 }
                 if (skip == 0) {
                     str.append(s.charAt(i));
                 }
             }
             return str.toString();
         }
 
         public ParsedProblem parseContent(ProblemRawData problem) {
             ParsedProblem p = new ParsedProblem(problem);
            String s = problem.getContent();
             s = s.replaceAll("&nbsp;", " ");
            String term = "<<<";
             String s2 = s.substring(s.indexOf(term));
             p.title = getTextBody(s2, false, 0);
            term = ">>>";
             String term2 = "<A NAME=\"SECTION0001001000000000000000\">";
            if (s.indexOf(term2) == -1) return p;
            s2 = s.substring(s.indexOf(term) + 3, s.indexOf(term2));
             p.condition = getTextBody(s2, true, 0);
             term = "<A NAME=\"SECTION0001002000000000000000\">";
             if (!s.contains(term)) return p;
             s2 = s.substring(s.indexOf(term2), s.indexOf(term));
             p.inputSpecification = getTextBody(s2, true, 1);
             term2 = "<A NAME=\"SECTION0001003000000000000000\">";
            if (s.indexOf(term2) == -1) return p;
             s2 = s.substring(s.indexOf(term), s.indexOf(term2));
             p.outputSpecification = getTextBody(s2, true, 1);
             return p;
         }
 }
