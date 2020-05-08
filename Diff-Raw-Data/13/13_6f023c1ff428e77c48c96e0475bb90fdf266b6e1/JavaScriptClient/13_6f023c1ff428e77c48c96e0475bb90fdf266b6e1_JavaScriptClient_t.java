 package nextgen.generator;
 
 import nextgen.dao.FileManager;
 import nextgen.model.Attribute;
 import nextgen.model.Element;
 import nextgen.model.Project;
 
 /**
  *
  * @author Mateo
  */
 public class JavaScriptClient extends Generator {
 
     private Project project;
     private String directory;
 
     @Override
     public void generate(Project project, String directory) throws Exception {
         this.project = project;
         this.directory = directory;
         String js = createDictionary() + "\n" + createModel() + "\n" + createCall();
        FileManager.generateFile(directory + "/" + project.getName() + ".js", js);
     }
 
     public String createDictionary() {
         String js = " // <editor-fold defaultstate=\"collapsed\" desc=\"Dictionary\">";
        js += "\nvar urlbase = '';// Webservice Base";
         for (Element e : project.getElements()) {
             js += "\nvar " + e.getName()+"s =  new Array();";
         }
         js += "\n// </editor-fold>";
         return js;
     }
 
     public String createModel() {
         String js = " // <editor-fold defaultstate=\"collapsed\" desc=\"Model\">";
         for (Element e : project.getElements()) {
             js += " \n// <editor-fold defaultstate=\"collapsed\" desc=\""+e.getName()+"\">";
             String params = "";
             String attributesInit = "";
             for (Attribute a : e.getAttributes()) {
                 if (!attributesInit.equals("")) {
                     params += ",";
                     attributesInit += "\n";
                 }
                 attributesInit = String.format(attributesInit + "\tthis.%s = %s;", a.getName(), a.getName());
                 params = String.format(params + "%s", a.getName());
             }
             js = String.format(js + "\n//%s"
                     + "\n\nfunction %s (%s){"
                     + "\n%s"
                     + "\n\tthis.create = function() {"
                     + "\n\t\tvar params = {command: \"create\", %s: JSON.stringify(this)};"
                     + "\n\t\tcallService(urlbase + \"/%sService.php\", params, \"register\", this);"
                     + "\n\t};"
                     + "\n\tthis.remove = function(callBack) {"
                     + "\n\t\tvar params = {command: \"delete\", %s: JSON.stringify(this)};"
                     + "\n\t\tcallService(urlbase + \"/%sService.php\", params, callBack, null);"
                     + "\n\t\t%ss[this.id] = undefined;"
                     + "\n\t};"
                     + "\n\tthis.update = function(callBack) {"
                     + "\n\t\tvar params = {command: \"modify\", %s: JSON.stringify(this)};"
                     + "\n\t\tcallService(urlbase + \"/%sService.php\", params, callBack, null);"
                     + "\n\t};"
                     + "\n\tthis.register = function(data) {"
                     + "\n\t\tthis.id = data;"
                     + "\n\t\t%ss[this.id] = this;"
                     + "\n\t};"
                     + "\n}"
                     + "\n%s.get = function(pages, counts, filter) {"
                     + "\n\tvar params = {command: \"get\", page: pages, count: counts, filters: JSON.stringify(filter)};"
                     + "\n\tcallService(urlbase + \"/%sService.php\", params, \"%s.init\", null);"
                    + "\n};"
                     + "\n%s.init = function(data) {"
                     + "\n\tfor (var i = 0; i < data.length; i++) {"
                     , e.getName(), e.getName(), params, attributesInit,e.getName(),e.getName()
                     , e.getName(),e.getName(),e.getName(),e.getName(),e.getName(),e.getName(),e.getName()
                     , e.getName(),e.getName(),e.getName());
             String init ="";
             for(Attribute a : e.getAttributes()){
                 if(!init.equals("")){
                     init += ",";
                 }
                 init += "data[i]."+a.getName();
             }
             js += "\n\t\t"+e.getName()+"s[data[i].id] = new "+e.getName()+"("+init+");"
                     + "\n\t}"
                    + "\n\tgetFinished(" + e.getName() + "s);"
                    + "\n};";
             js += "\n// </editor-fold>";
         }
         js += "\n// </editor-fold>";
         return js;
     }
 
     public String createCall() {
         String call = "\n\n// <editor-fold defaultstate=\"collapsed\" desc=\"callService\">\n"
                 + "function callService(urlService, args, callBackFunction, element) {\n"
                 + "\t$.ajax({\n"
                 + "\t\tdataType: \"jsonp\",\n"
                 + "\t\turl: urlService,\n"
                 + "\t\tdata: args,\n"
                 + "\t\ttype: \"GET\",\n"
                 + "\t\tcrossDomain: true,\n"
                 + "\t\tsuccess: function(data) {\n"
                 + "\t\t\tif (element != null) {\n"
                 + "\t\t\t\telement[callBackFunction](data);\n"
                 + "\t\t\t} else {\n"
                 + "\t\t\t\teval(callBackFunction)(data);\n"
                 + "\t\t\t}\n"
                 + "}\t\t, error: function(e, xhr) {\t"
                 + "\t\t\tconsole.log(e);\n"
                 + "\t\t}});\n"
                 + "}"
                 + "\n// </editor-fold>";
         return call;
     }
 }
