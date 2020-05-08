 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.anzix.snipsnap2xwiki.migrator;
 
 import net.anzix.snipsnap2xwiki.MigrationContext;
 import net.anzix.snipsnap2xwiki.MigrationSet;
 import net.anzix.snipsnap2xwiki.Page;
 import net.anzix.snipsnap2xwiki.transformation.DateTransformer;
 import org.jdom.Element;
 
 import java.util.*;
 
 /**
  * Migrate Snips (wiki pages).
  */
 public class SnipMigrator extends AbstractObjectMigrator {
 
     private boolean migrateAttachments = true;
 
     private Set<String> ignorePages;
 
     private MigrationSet steps = new MigrationSet();
 
     private Outputter output;
 
     private List<Page> pagesToMigrate = new ArrayList<Page>();
     private Map<String, Page> lastSaved = new HashMap<String, Page>();
     private Map<String, Integer> lastVersion = new HashMap<String, Integer>();
 
     public SnipMigrator(MigrationContext context, Outputter output) {
         super(context);
         this.output = output;
 
 
         ignorePages = new HashSet();
         ignorePages.add("start");
         ignorePages.add("start2");
         ignorePages.add("sandbox");
         ignorePages.add("snips-by-user");
         //todo
         //ignorePages.add("JMX");
         //ignorePages.add("singleton");
         ignorePages.add("WTF/Xmas");
         ignorePages.add("Xmas");
         ignorePages.add("archivum");
         ignorePages.add("oldposts");
 
 
         steps.add(new MigrationStep("name", "path"));
 
         steps.add(new MigrationStep("name", "title"));
         steps.add(new MigrationStep("mUser", "author"));
         steps.add(new MigrationStep("cUser", "creator"));
         steps.add(new MigrationStep("version", "version"));
 
         steps.add(new MigrationStep("cTime", "creationDate", new DateTransformer()));
         steps.add(new MigrationStep("mTime", "date", new DateTransformer()));
 
         steps.add(new MigrationStep("content", "content", getContext().getSyntaxTransformation()));
 
 
     }
 
     @Override
     protected boolean includeInMigration(Element e) {
 
         if (e.getName().equals("user")) {
             return false;
         }
         String name = e.getChildText("name");
         if (name.equals("admin")) {
             return false;
         } else if (name.startsWith("comment") || name.startsWith("start/") || name.startsWith("topics/")) {
             return false;
         } else if (name.startsWith("SnipSnap") || name.startsWith("snipsnap")) {
             return false;
         } else if (ignorePages.contains(name)) {
             return false;
         } else if (getContext().getUserCache().containsKey(name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public void migrateObject(Element oldRoot) throws Exception {
 
         String name = oldRoot.getChildText("name");
 
         //TODO: Arvizturo tukorfurogep
         if (name.contains("zt") && name.contains("rf")) {
             return;
         }
         System.out.println("Migrating " + name);
 
         //copy properties
         Page mainPage = createPage(oldRoot);
         mainPage.setLatest(true);
         mainPage.addMeta("name", name);
         pagesToMigrate.add(mainPage);
 
 
         Element version = oldRoot.getChild("versions");
         if (version != null) {
             for (Object change : version.getChildren("snip")) {
 
                 Page older = createPage((Element) change);
                 older.addMeta("name", name);
                 older.addMeta("creator", (String) mainPage.getMeta("creator"));
                 older.addMeta("creationDate", (String) mainPage.getMeta("creationDate"));
                 pagesToMigrate.add(older);
             }
         }
 
     }
 
     private Page createPage(Element oldRoot) {
         Page newPage = new Page();
         newPage.addMeta("layout", "wiki");
 
         String name = oldRoot.getChildText("name");
         String cleanName = "";
         int i = 0;
         while (i < name.length()) {
             char ch = name.charAt(i);
             if ((ch & 0xC0) == 0xA0) {
                 cleanName += "x";
             } else {
                 cleanName += ch;
             }
             i++;
         }
         name = cleanName;
         steps.migrate(oldRoot, newPage);
         return newPage;
     }
 
     @Override
     public void migrate(Element root) throws Exception {
         super.migrate(root);
         Page p = new Page();
         p.addMeta("title", "TODO");
         p.addMeta("name", "TODO");
         p.addMeta("layout", "default");
         String content = "# TODO\n\n";
         for (String missing : getContext().getMissingPages()) {
             content += "-  " + missing + "\n";
         }
         p.setContent(content);
         output.write(p);
 
 
     }
 
     @Override
     protected void migrationEnd() {
         Collections.sort(pagesToMigrate, new Comparator<Page>() {
             @Override
             public int compare(Page o1, Page o2) {
                 return o1.getDate().compareTo(o2.getDate());
             }
         });
         for (Page p : pagesToMigrate) {
             int version = 0;
             if (p.getMeta("version") != null) {
                 version = Integer.parseInt((String) p.getMeta("version"));
             } else if (lastVersion.containsKey(p.getName())) {
                 version = lastVersion.get(p.getName()) + 1;
             }
             lastVersion.put(p.getName(), version);
             p.setVersion(version);
             output.write(p);
         }
 
     }
 
     public void setMigrateAttachments(boolean migrateAttachments) {
         this.migrateAttachments = migrateAttachments;
     }
 
 }
