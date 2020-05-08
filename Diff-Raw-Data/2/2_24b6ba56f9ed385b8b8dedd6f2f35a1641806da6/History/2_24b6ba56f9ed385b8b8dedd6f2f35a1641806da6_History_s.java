 package mediateka.db;
 
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import mediateka.datamanagers.Condition;
 import mediateka.datamanagers.Managers;
 import org.dom4j.Element;
 import org.dom4j.tree.DefaultElement;
 
 /**
  * Класс, представляющий таблицу истории
  * @author Alexandr
  */
 public class History extends Table {
 
     public History() {
         tableName = "history";
     }
 
     public boolean load(String fileName) throws LoadException {
         try {
             DefaultElement root = super.getRootElement(fileName);
             SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
             for (Iterator<Element> it = root.elements().iterator(); it.hasNext();) {
                 DefaultElement elem = (DefaultElement) it.next();
                 recordsList.add(new HistoryRecord(
                         Integer.parseInt(elem.attribute("id").getValue()),
                         (Disc) Managers.getInstance().getDiscsManager().find(Integer.parseInt(elem.node(0).getText())),
                         (Person) Managers.getInstance().getPersManager().find(Integer.parseInt(elem.node(1).getText())),
                         format.parse(elem.node(3).getText()),
                         format.parse(elem.node(5).getText()),
                         format.parse(elem.node(4).getText()),
                         elem.node(2).getText()));
             }
             return true;
         } catch (Exception ex) {
             throw new LoadException("История не загружена!");
         }
     }
 
     public Records find(Record record) {
         History retVal = new History();
         HashMap<String, String> map = new HashMap<String, String>();
         HistoryRecord rec;
         try {
             rec = (HistoryRecord) record;
         } catch (Exception e) {
             return null;
         }
         if (rec.getID() > 0) {
             map.put("id", Integer.toString(rec.getID()));
         } else {
             if (rec.getDisc() != null) {
                 map.put("discID", Integer.toString(rec.getDisc().getID()));
             }
             if (rec.getPerson() != null) {
                 map.put("personID", Integer.toString(rec.getPerson().getID()));
             }
             if (!rec.getComment().equals("")) {
                 map.put("comment", rec.getComment());
             }
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
             if (rec.getGiveDate() != null) {
                 map.put("dateOfIssue", formatter.format(rec.getGiveDate()));
             }
             if (rec.getReturnDate() != null) {
                 map.put("dateOfReturn", formatter.format(rec.getReturnDate()));
             }
             if (rec.getPromisedDate() != null) {
                 map.put("promisingDate", formatter.format(rec.getPromisedDate()));
             }
         }
        Condition cond = new Condition(map, Managers.mode);
         for (Record historyRecord : recordsList) {
             if (cond.isEquals(historyRecord)) {
                 retVal.add(historyRecord);
             }
         }
         return retVal.size() > 0 ? retVal : null;
     }
 
     protected Record createRecord(int id) {
         return new HistoryRecord(id);
     }
 }
