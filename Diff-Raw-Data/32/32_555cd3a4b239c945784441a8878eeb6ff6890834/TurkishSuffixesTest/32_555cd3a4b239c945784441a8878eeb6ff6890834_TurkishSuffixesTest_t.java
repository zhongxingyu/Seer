 package zemberek3.lexicon;
 
 import junit.framework.Assert;
 import org.junit.Test;
 import zemberek3.lexicon.graph.DynamicLexiconGraph;
 import zemberek3.parser.morphology.ParseResult;
 import zemberek3.parser.morphology.SimpleParser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TurkishSuffixesTest {
     @Test
     public void testVoicing() {
         DynamicLexiconGraph graph = getLexiconGraph("armut");
         assertHasParses(graph, "armut", "armuda", "armutlar", "armutlara");
         assertUnParseable(graph, "armud", "armuta", "armudlar");
     }
 
     @Test
     public void testCompounds() {
         DynamicLexiconGraph graph = getLexiconGraph("zeytinyağı [A:CompoundP3sg ;R:zeytinyağ]");
         assertHasParses(graph, "zeytinyağcık", "zeytinyağım", "zeytinyağına", "zeytinyağı", "zeytinyağcığa", "zeytinyağlarım");
         assertUnParseable(graph, "zeytinyağılar", "zeytinyağıcık");
     }
 
     @Test
     public void testCompoundsVoicing() {
         DynamicLexiconGraph graph = getLexiconGraph("atkuyruğu [A:CompoundP3sg, Voicing ; R:atkuyruk]");
         assertHasParses(graph, "atkuyrukçuk", "atkuyruğu", "atkuyruklarım");
         assertUnParseable(graph, "atkuyruğlarım", "atkuyruk");
     }
 
     @Test
     public void testWithAndWithout() {
         DynamicLexiconGraph graph = getLexiconGraph("elma", "kitap");
         assertHasParses(graph, "elmalı", "elmasız", "kitaplı", "kitapsız");
         assertUnParseable(graph, "elmayalı", "elmalarlı", "elmadasız", "elmalarsız");
     }
 
     @Test
     public void testBecome() {
         // noun
         DynamicLexiconGraph graph = getLexiconGraph("elma", "mavi [P:Adj]");
         assertHasParses(graph, "elmalaş");
         // adj
         assertHasParses(graph, "mavileş");
     }
 
     @Test
     public void testNounToVerbCopular() {
         DynamicLexiconGraph graph = getLexiconGraph("elma");
         assertHasParses(graph, "elmaydı");
     }
 
     @Test
     public void testAdj2Verb() {
         DynamicLexiconGraph graph = getLexiconGraph("mavi [P:Adj]");
         assertHasParses(graph, "mavileşti", "mavileşmiş", "maviydi");
     }
 
 
     @Test
     public void testAdj2Noun() {
         // noun
         DynamicLexiconGraph graph = getLexiconGraph("mavi [P:Adj]");
         assertHasParses(graph, "maviye", "mavilerde");
     }
 
     @Test
     public void testQuiteAndLy() {
         // Adj-Adj and Adj-Adv
         DynamicLexiconGraph graph = getLexiconGraph("hızlı [P:Adj]");
         assertHasParses(graph, "hızlıca");
     }
 
     @Test
     public void testRelated() {
         // Noun-Adj
         DynamicLexiconGraph graph = getLexiconGraph("elma");
         assertHasParses(graph, "elmadaki", "elmadakini");
         assertUnParseable(graph, "elmaki", "elmayaki", "elmadakiki");
         //TODO: add akşamki etc. uses Rel_kI instead of Rel_ki
     }
 
     @Test
     public void testDim() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("armut");
         assertHasParses(graph, "armutçuk", "armutçuğu", "armutcağız", "armutcağızı");
         assertUnParseable(graph, "armutçuğ", "armutçukcuk", "armutçukcağız", "armutcağızcık");
         // TODO: check oflazer parse "babacım = baba+Noun+A3sg+Pnon+Nom^DB+Noun+Dim+A3sg+P1sg+Nom
     }
 
     @Test
     public void testResembl() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("armut", "yeşil [P:Adj]");
         assertHasParses(graph, "armutsu", "armudumsu", "yeşilsi", "yeşilimsi");
         // TODO: oflazer uses JustLike for this. It parses words like "tuhafsı","arabası" as JustLike
     }
 
     @Test
     public void testCausative() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("yapmak", "aramak");
         assertHasParses(graph, "yaptır", "yaptırt", "yaptırttır", "arat", "arattır", "arattırt");
         assertUnParseable(graph, "yapt", "aratt");
     }
 
     @Test
     public void testPassive() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("yapmak", "aramak", "gelmek");
         assertHasParses(graph, "aranıl", "yapıl", "aran", "gelin", "gelinil");
         assertUnParseable(graph, "aral", "gelil", "kavurul", "kavurunul");
         // causative and passive
         assertHasParses(graph, "yaptırıl", "yaptırtıl", "yaptırttırıl", "aratıl", "arattırıl", "arattırtıl");
     }
 
     @Test
     public void testNegative() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gelmek");
         assertHasParses(graph, "arama", "gelme");
     }
 
     @Test
     public void testPrograesive() {
         // Noun-Noun
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gelmek");
         assertHasParses(graph, "arıyor", "aramıyor", "geliyor", "gelmiyor");
         assertHasParses(graph, "aramakta", "aramamakta", "gelmekte", "gelmemekte");
     }
 
     @Test
     public void testAorist() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]", "gelmek [A:NonTransitive, Aorist_I]");
         assertHasParses(graph, "arar", "ararsa", "gider", "gelir", "aramaz");
         assertUnParseable(graph, "geler", "gidir");
     }
 
     @Test
     public void testPast() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "aradı", "gitti", "gittik", "gittiyse", "gittim", "gittiniz", "gittiydim");
         assertUnParseable(graph, "gittiz", "gittimiş");
     }
 
     @Test
     public void testImp() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
        assertHasParses(graph, "ara", "git", "gitme", "gidiniz", "gitsene", "gitsenize", "aramayacak", "aramasın", "gitmesin", "arasınlar", "gitmesinler");
        assertHasParses(graph, "aramasanıza", "aramayın", "gitmeyin");
     }
 
     @Test
     public void testEvid() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "aramış", "gitmiş", "gitmişiz", "aramıştı");
     }
 
     @Test
     public void testVerb2Verb2() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "arayıver", "aramayıver", "gidiver", "gitmeyiver");
     }
 
     @Test
     public void testInf() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "aramak", "aramada", "arayışı", "gitmek", "gitmekten", "gitmektendi");
     }
 
     @Test
     public void testFuture() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "arayacak", "arayacağız", "aramayacak", "aratacak", "arayacaklar", "arayacaktık");
         assertHasParses(graph, "gidecek", "gideceğiz", "gitmeyecek", "gidecekler", "gidecektik");
     }
 
 
     @Test
     public void testNess() {
         DynamicLexiconGraph graph = getLexiconGraph("elma", "mavi [P:Adj]");
         assertHasParses(graph, "elmacıktı", "elmalık", "elmalığı", "elmalıktı", "elmalığa", "mavilik", "maviliği", "mavilikti");
         assertUnParseable(graph, "elmalıklık", "elmalıka", "maviliklik", "maviliki");
     }
 
     @Test
     public void verbVowelDrop() {
         DynamicLexiconGraph graph = getLexiconGraph("kavurmak [A:LastVowelDrop]");
         assertHasParses(graph, "kavurdu", "kavuracağız", "kavurur", "kavuruyor", "kavur", "kavrul", "kavrulma", "kavurma", "kavrulacağız", "kavrulmayıver", "kavurtacağız");
         assertUnParseable(graph, "kavurul", "kavracağız", "kavruyor", "kavrar", "kavrar");
     }
 
     @Test
     public void nounVowelDrop() {
         DynamicLexiconGraph graph = getLexiconGraph("ağız [A:LastVowelDrop]");
         assertHasParses(graph, "ağız", "ağzı", "ağızlar", "ağza");
         assertUnParseable(graph, "ağızı", "ağzlar");
     }
 
     @Test
     public void optative() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         assertHasParses(graph, "araya", "arayayım", "gide", "gidesin");
     }
 
     @Test
    public void necessity() {
        DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
        assertHasParses(graph, "gitmeli", "gitmeliyim", "gitmemeli", "gitmemelisiniz", "gitmeliyiz");
        assertHasParses(graph, "aramalı", "aramamalı", "aramalıyım", "aramamalısın", "aramamalısınız");
    }

    @Test
     public void ability() {
         DynamicLexiconGraph graph = getLexiconGraph("aramak", "gitmek [A:Voicing, Aorist_A]");
         // abil
         assertHasParses(graph, "arayabil", "arayabilir", "arayabilecek", "gidebil", "gidebilecek", "gidebiliyor");
         // a
         assertHasParses(graph, "arayama", "arayamayabilir", "gideme", "gidemeyebilir");
 
     }
 
 
 
     private synchronized void assertHasParses(DynamicLexiconGraph graph, String... words) {
         SimpleParser parser = new SimpleParser(graph);
         for (String word : words) {
             List<ParseResult> results = parser.parse(word);
             if (results.size() == 0)
                 parser.dump(word);
             Assert.assertTrue("No parse for:" + word, results.size() > 0);
             for (ParseResult result : results) {
                 System.out.println(word + "= " + result.asParseString());
             }
         }
     }
 
     private synchronized void assertUnParseable(DynamicLexiconGraph graph, String... words) {
         SimpleParser parser = new SimpleParser(graph);
         for (String word : words) {
             List<ParseResult> results = parser.parse(word);
             Assert.assertTrue("Unexpected parse for:" + word + " parse:" + results, results.size() == 0);
         }
     }
 
     private synchronized DynamicLexiconGraph getLexiconGraph(String... words) {
         SuffixProvider suffixProvider = new TurkishSuffixes();
         List<DictionaryItem> items = getItems(words, suffixProvider);
         DynamicLexiconGraph graph = new DynamicLexiconGraph(suffixProvider);
         graph.addDictionaryItems(items);
         return graph;
     }
 
     private synchronized List<DictionaryItem> getItems(String[] lines, SuffixProvider suffixProvider) {
         TurkishDictionaryLoader loader = new TurkishDictionaryLoader(suffixProvider);
         List<DictionaryItem> items = new ArrayList<DictionaryItem>();
         for (String line : lines) {
             items.add(loader.loadFromString(line));
         }
         return items;
     }
 }
