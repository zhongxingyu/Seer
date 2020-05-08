 package mediateka.db;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.dom.DOMElement;
 
 /**
  * Класс, представляющий запись в таблице истории
  * @author DeKaN
  */
 public class HistoryRecord implements Record {
 
     private int recordID;
     private Disc disc;
     private Person person;
     private Date giveDate;
     private Date promisedDate;
     private Date returnDate = null;
     private String comment = "";
 
     /**
      * Получить ID записи
      * @return ID записи
      */
     public int getID() {
         return this.recordID;
     }
 
     /**
      * Получить диск
      * @return Диск
      */
     public Disc getDisc() {
         return this.disc;
     }
 
     /**
      * Установить диск
      * @param disc Диск, который будет установлен
      */
     public void setDisc(Disc disc) {
         if (disc == null) {
             throw new NullPointerException();
         }
         this.disc = disc;
     }
 
     /**
      * Получить персональные данные
      * @return Персональные данные
      */
     public Person getPerson() {
         return this.person;
     }
 
     /**
      * Установить персональные данные
     * @param disc Персональные данные, который будет установлены
      */
     public void setPerson(Person person) {
         if (person == null) {
             throw new NullPointerException();
         }
         this.person = person;
     }
 
     /**
      * Получить дату, когда диск отдан
      * @return Дата, когда диск отдан
      */
     public Date getGiveDate() {
         return this.giveDate;
     }
 
     /**
      * Установить дату, когда диск отдан
     * @param disc Новая дата, когда диск отдан
      */
     public void setGiveDate(Date giveDate) {
         if (giveDate == null) {
             throw new NullPointerException();
         }
         this.giveDate = giveDate;
     }
 
     /**
      * Получить дату, когда диск обещали вернуть
      * @return Дата, когда диск обещали вернуть
      */
     public Date getPromisedDate() {
         return this.promisedDate;
     }
 
     /**
      * Установить дату, когда диск обещали вернуть
     * @param disc Новая дата, когда диск обещали вернуть
      */
     public void setPromisedDate(Date promisedDate) {
         if (promisedDate == null) {
             throw new NullPointerException();
         }
         this.promisedDate = promisedDate;
     }
 
     /**
      * Получить дату, когда диск вернули
      * @return Дата, когда диск вернули
      */
     public Date getReturnDate() {
         return this.returnDate;
     }
 
     /**
      * Установить дату, когда диск вернули
     * @param disc Новая дата, когда диск вернули
      */
     public void setReturnDate(Date returnDate) {
         this.returnDate = returnDate;
     }
 
     /**
      * Получить комментарий
      * @return Комментарий
      */
     public String getComment() {
         return this.comment;
     }
 
     /**
      * Установить комментарий
     * @param disc Комментарий, который будет установлен
      */
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     /**
      * Конструктор по обязательным параметрам
      * @param disc Диск, который отдали
      * @param person Человек, которому отдали диск
      * @param give Дата, когда отдан диск
      * @param promise Дата, когда диск обещали вернуть
      */
     public HistoryRecord(Disc disc, Person person, Date give, Date promise) {
         this(disc, person, give, promise, null, "");
     }
 
     /**
      * Полный конструктор
      * @param disc Диск, который отдали
      * @param person Человек, которому отдали диск
      * @param give Дата, когда отдан диск
      * @param promise Дата, когда диск обещали вернуть
      * @param returned Дата, когда диск вернули
      * @param comment Комментарий
      */
     public HistoryRecord(Disc disc, Person person, Date give, Date promise, Date returned, String comment) {
         if ((disc == null) || (person == null) || (give == null) || (promise == null)) {
             throw new NullPointerException();
         }
         this.disc = disc;
         this.person = person;
         this.giveDate = give;
         this.promisedDate = promise;
         this.returnDate = returned;
         this.comment = comment;
     }
 
     /**
      * Сериализует запись в XML
      * @return Строка с записью, сериализованной в XML element
      */
     public String ToXmlElement() {
         Element elem = new DOMElement("record");
         elem.addAttribute("recordID", Integer.toString(this.recordID));
         elem.addElement("discID").addText(Integer.toString(this.disc.getID()));
         elem.addElement("personID").addText(Integer.toString(this.person.getID()));
         elem.addElement("comment").addText(this.comment);
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         elem.addElement("dateOfIssue").addText(formatter.format(this.giveDate));
         elem.addElement("dateOfReturn").addText(this.returnDate == null
                 ? "" : formatter.format(this.returnDate));
         elem.addElement("promisingDate").addText(formatter.format(this.promisedDate));
         return elem.asXML();
     }
 }
