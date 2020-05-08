 
 import java.util.ArrayList;
 
 class document {
 
     document(Integer genreID) {
         this();//TODO add genre_id to database?
     }
 
     public ArrayList<Integer> getAuthorsIDs() {
         return authorsIDs;
     }
 
     public void addAuthorsIDs(Integer peopleID) {
         authorsIDs.add(peopleID);
     }
     private String title;
     private Integer start_page;
     private Integer end_page;
     private Integer year;
     private Integer volume;
     private Integer number;
     private String url;
     private String ee;
     private String cdrom;
     private String cite;
     private String crossref;
     private String isbn;
     private String series;
     private ArrayList<Integer> authorsIDs;
     private Integer editor_id;
     private Integer booktitle_id;
     private Integer publisher_id;
 
     public document() {
         authorsIDs = new ArrayList<Integer>();
     }
 
     String getColumns() {
         String col = "";
         if (title != null) {
             col += "title,";
         }
         if (start_page != null) {
             col += "start_page,";
         }
         if (end_page != null) {
             col += "end_page,";
         }
         if (year != null) {
             col += "year,";
         }
         if (volume != null) {
             col += "volume,";
         }
         if (number != null) {
             col += "number,";
         }
         if (url != null) {
             col += "url,";
         }
         if (ee != null) {
             col += "ee,";
         }
         if (cdrom != null) {
             col += "cdrom,";
         }
         if (cite != null) {
             col += "cite,";
         }
         if (crossref != null) {
             col += "crossref,";
         }
         if (isbn != null) {
             col += "isbn,";
         }
         if (series != null) {
             col += "series,";
         }
         if (editor_id != null) {
             col += "editor_id,";
         }
         if (booktitle_id != null) {
             col += "booktitle_id,";
         }
         if (publisher_id != null) {
             col += "publisher_id,";
         }
 
         if (!col.isEmpty()) {
             return col.substring(0, col.length() - 1);
         }
         return "";
     }
 
     String getValues() {
         String val = "";
         if (title != null) {
             val += "'" + cleanSQL(title) + "',";
         }
         if (start_page != null) {
             val += "'" + start_page + "',";
         }
         if (end_page != null) {
             val += "'" + end_page + "',";
         }
         if (year != null) {
             val += "'" + year + "',";
         }
         if (volume != null) {
             val += "'" + volume + "',";
         }
         if (number != null) {
             val += "'" + number + "',";
         }
         if (url != null) {
             val += "'" + cleanSQL(url) + "',";
         }
         if (ee != null) {
             val += "'" + cleanSQL(ee) + "',";
         }
         if (cdrom != null) {
             val += "'" + cleanSQL(cdrom) + "',";
         }
         if (cite != null) {
             val += "'" + cleanSQL(cite) + "',";
         }
         if (crossref != null) {
             val += "'" + cleanSQL(crossref) + "',";
         }
         if (isbn != null) {
             val += "'" + cleanSQL(isbn) + "',";
         }
         if (series != null) {
             val += "'" + series + "',";
         }
         if (editor_id != null) {
             val += "'" + editor_id + "',";
         }
         if (booktitle_id != null) {
             val += "'" + booktitle_id + "',";
         }
         if (publisher_id != null) {
             val += "'" + publisher_id + "',";
         }
 
         if (!val.isEmpty()) {
             return val.substring(0, val.length() - 1);
         }
         return "";
     }
 
     public Integer getBooktitle_id() {
         return booktitle_id;
     }
 
     public void setBooktitle_id(Integer booktitle_id) {
         if (this.booktitle_id == null) {
             this.booktitle_id = booktitle_id;
         } else {
             System.out.println("Multiple booktitle_id: " + booktitle_id);
         }
     }
 
     public String getCdrom() {
         return cdrom;
     }
 
     public void setCdrom(String cdrom) {
         this.cdrom = cdrom;
     }
 
     public String getCite() {
         return cite;
     }
 
     public void setCite(String cite) {
         if (this.cite == null) {
             this.cite = cite;
         } else {
             System.out.println("Multiple cite: " + cite);
         }
     }
 
     public String getCrossref() {
         return crossref;
     }
 
     public void setCrossref(String crossref) {
         if (this.crossref == null) {
             this.crossref = crossref;
         } else {
             System.out.println("Multiple crossref: " + crossref);
         }
     }
 
     public Integer getEditor_id() {
         return editor_id;
     }
 
     public void setEditor_id(Integer editor_id) {
         if (this.editor_id == null) {
             this.editor_id = editor_id;
         } else {
             System.out.println("Multiple editor_id: " + editor_id);
         }
     }
 
     public String getEe() {
         return ee;
     }
 
     public void setEe(String ee) {
         if (this.ee == null) {
             this.ee = ee;
         } else {
             System.out.println("Multiple ee: " + ee);
         }
     }
 
     public Integer getEnd_page() {
         return end_page;
     }
 
     public void setPages(String pages) {
        //TODO Split page string to start and end
         String page[] = pages.split("-");
        this.start_page = Integer.parseInt(page[0]);
        this.end_page = Integer.parseInt(page[1]);
     }
 
     public String getIsbn() {
         return isbn;
     }
 
     public void setIsbn(String isbn) {
         if (this.isbn == null) {
             this.isbn = isbn;
         } else {
             System.out.println("Multiple isbn: " + isbn);
         }
     }
 
     public Integer getNumber() {
         return number;
     }
 
     public void setNumber(Integer number) {
         if (this.number == null) {
             this.number = number;
         } else {
             System.out.println("Multiple number: " + number);
         }
     }
 
     public Integer getPublisher_id() {
         return publisher_id;
     }
 
     public void setPublisher_id(Integer publisher_id) {
         if (this.publisher_id == null) {
             this.publisher_id = publisher_id;
         } else {
             System.out.println("Multiple publisher_id: " + publisher_id);
         }
     }
 
     public String getSeries() {
         return series;
     }
 
     public void setSeries(String series) {
         if (this.series == null) {
             this.series = series;
         } else {
             System.out.println("Multiple series: " + series);
         }
     }
 
     public Integer getStart_page() {
         return start_page;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         if (this.title == null) {
             this.title = title;
         } else {
             System.out.println("Multiple title: " + title);
         }
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         if(this.url == null){
             this.url = url;
         } else {
             System.out.println("Multiple url: " + url);
         }
     }
 
     public Integer getVolume() {
         return volume;
     }
 
     public void setVolume(Integer volume) {
         if(this.volume == null){
             this.volume = volume;
         } else {
             System.out.println("Multiple volume: " + volume);
         }
     }
 
     public Integer getYear() {
         return year;
     }
 
     public void setYear(Integer year) {
         if(this.year == null){
         this.year = year;
         } else {
             System.out.println("Multiple year: " + year);
         }
     }
 
     public static String cleanSQL(String arg) {
         String rtn = arg.replace("\\", "\\\\");
         return rtn.replace("'", "''");
     }
 }
