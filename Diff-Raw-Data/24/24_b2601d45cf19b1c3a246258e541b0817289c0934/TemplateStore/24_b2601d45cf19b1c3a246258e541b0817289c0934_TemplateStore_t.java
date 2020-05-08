 package edu.ualberta.med.biobank.barcodegenerator.template;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class TemplateStore implements Serializable {
 
     private static final long serialVersionUID = 5502373669875110097L;
 
     ArrayList<Template> templates;
 
    public TemplateStore() {
         try {
             loadStore(new File("Store.dat"));

         } catch (IOException e) {
             System.err.println("WARNING: store.dat file does not exist!");
            templates = new ArrayList<Template>();

        } catch (ClassNotFoundException e) {
            System.err
                .println("WARNING: could not load template store from store.dat");
            templates = new ArrayList<Template>();
         }
     }
 
     public String[] getTemplateNames() {
         String[] templateNames = new String[templates.size()];
 
         int i = 0;
         for (Template t : templates) {
             templateNames[i] = t.getName();
             i++;
         }
         return templateNames;
     }
 
     public Template getTemplate(String name) {
         for (Template t : templates) {
             if (name.equals(t.getName()))
                 return t;
         }
         return null;
     }
 
     // false if the template you wish to remove does not exist in the store.
     public boolean removeTemplate(String name) {
         Template targetTemplate = null;
         for (Template t : templates) {
             if (name.equals(t.getName())) {
                 targetTemplate = t;
                 break;
             }
         }
 
         if (targetTemplate != null) {
             templates.remove(targetTemplate);
 
             try {
                 saveStore(new File("Store.dat"));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return true;
         }
 
         return false;
     }
 
     public boolean updateTemplate(Template updatedTemplate) throws IOException {
 
         Template oldTemplate = getTemplate(updatedTemplate.getName());
 
         if (oldTemplate == null)
             return false;
 
         removeTemplate(oldTemplate);
         addTemplate(updatedTemplate);
 
         saveStore(new File("Store.dat"));
 
         return true;
     }
 
     public boolean removeTemplate(Template template) {
         return removeTemplate(template.getName());
 
     }
 
     // false if a template with the same name was found
     public boolean addTemplate(Template template) {
         for (String name : getTemplateNames()) {
             if (name.equals(template.getName())) {
                 return false;
             }
         }
         templates.add(template);
 
         try {
             saveStore(new File("Store.dat"));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return true;
     }
 
     private void loadStore(File file) throws IOException,
         ClassNotFoundException {
         TemplateStore templateStore = null;
         FileInputStream fis = null;
         ObjectInputStream in = null;
         fis = new FileInputStream(file);
         in = new ObjectInputStream(fis);
         templateStore = (TemplateStore) in.readObject();
         in.close();
         this.templates = templateStore.templates;
     }
 
     private void saveStore(File file) throws IOException {
         FileOutputStream fos = null;
         ObjectOutputStream out = null;
         fos = new FileOutputStream(file);
         out = new ObjectOutputStream(fos);
         out.writeObject(this);
         out.close();
     }
 
 }
