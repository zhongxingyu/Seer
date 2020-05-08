 package weigl.grammar.lltck;
 
import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.List;
 
import weigl.io.file.FileUtils;

 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 
 public class TokenParserGenerator
 {
     public static final String   CLAZZ_NAME     = "TParser";
     public static final String   CONANCIAL_NAME = "weigl.grammar.lltck." + CLAZZ_NAME;
     public static final String[] CLASSES_NAMES  = new String[] { CONANCIAL_NAME,
                     CONANCIAL_NAME + "$Rule" + CLAZZ_NAME + "Callback",
                     CONANCIAL_NAME + "$Rule" + CLAZZ_NAME + "CallbackAdapter",
                     CONANCIAL_NAME + "$Token" + CLAZZ_NAME + "Callback",
                     "weigl.grammar.lltck.TokenTParser" };
 
     private String               source;
     private TokenGrammarParser   stx;
     private List<SynRule>        rules;
     private List<SynToken>       tokens;
 
     public TokenParserGenerator(String text) throws LeftRecursionException, RuleUnknownException
     {
         source = text;
         parse();
     }
 
     private void parse() throws LeftRecursionException, RuleUnknownException
     {
         stx = new TokenGrammarParser(source);
         rules = stx.getRules();
         tokens = stx.getTokens();
         GrammarAlgorithms.check(rules);
 
         for (SynToken e : tokens)
             System.out.format("%-20s = %20s%n", e.name, e.regex);
 
         for (SynRule e : rules)
         {
             System.out.format("%-20s%n", e.name);
             for (SynDerivation d : e.getDerivations())
             {
                 System.out.format("\t%-20s%n", d.getTokenList().toString());
                 System.out.format("\t\t%20s%n", d.getFirstTokens().toString());
             }
         }
     }
 
     public String getJavaSource()
     {
         try
         {
 
             Configuration cfg = new Configuration();
             cfg.setObjectWrapper(DefaultObjectWrapper.DEFAULT_WRAPPER);
             cfg.setDefaultEncoding("UTF-8");
 
             Reader reader = new InputStreamReader(getClass().getResourceAsStream("Parser.ftl"));
 
             Template tpl;
             tpl = new Template("Parser", reader, cfg);
 
             SimpleHash rootMap = new SimpleHash();
             rootMap.put("classname", CLAZZ_NAME);
             rootMap.put("tokens", tokens);
             rootMap.put("rules", rules);
             rootMap.put("input", source);
 
             StringWriter output = new StringWriter(1024);
             tpl.process(rootMap, output);
             return output.getBuffer().toString();
         } catch (Exception e)
         {
             e.printStackTrace();
         }
         return "";
     }
    
    public static void main(String[] args) throws IOException, LeftRecursionException, RuleUnknownException
    {
        String s = "$whitespace = \\s\n"+
        "digits = \\d+\n"+
        "letters = \\w+\n"+
        "\n"+
        "START: digits letters digits";
        
        TokenParserGenerator tgp = new TokenParserGenerator(s);
        FileUtils.writeTo("src/weigl/grammar/lltck/TParser.java",tgp.getJavaSource() );
    }
 
     public String getClassName()
     {
         return CLAZZ_NAME;
     }
 }
