 //
 // Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
 // Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
 // nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.06.23 um 03:12:10 PM CEST 
 //
 
 
 package djproject.songs;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.XmlType;
 
 
 /**
  * <p>Java-Klasse fr anonymous complex type.
  * 
  * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
  * 
  * <pre>
  * &lt;complexType>
  *   &lt;complexContent>
  *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
  *       &lt;sequence>
  *         &lt;element ref="{}id"/>
  *         &lt;element ref="{}artist"/>
  *         &lt;element ref="{}title"/>
  *         &lt;element ref="{}length"/>
  *         &lt;element ref="{}album" minOccurs="0"/>
  *         &lt;element ref="{}album_cover" minOccurs="0"/>
  *         &lt;element ref="{}album_artist" minOccurs="0"/>
  *         &lt;element ref="{}number_in_album" minOccurs="0"/>
  *         &lt;element ref="{}genre"/>
  *       &lt;/sequence>
  *     &lt;/restriction>
  *   &lt;/complexContent>
  * &lt;/complexType>
  * </pre>
  * 
  * 
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "", propOrder = {
     "id",
     "artist",
     "title",
     "length",
     "album",
     "albumCover",
     "albumArtist",
     "numberInAlbum",
     "genre"
 })
 @XmlRootElement(name = "song")
 public class Song {
 
     protected int id;
     @XmlElement(required = true)
     protected String artist;
     @XmlElement(required = true)
     protected String title;
     protected int length;
     protected String album;
     @XmlElement(name = "album_cover")
    @XmlSchemaType(name = "anyURI")
     protected String albumCover;
     @XmlElement(name = "album_artist")
     protected String albumArtist;
     @XmlElement(name = "number_in_album")
     protected Integer numberInAlbum;
     @XmlElement(required = true)
     protected String genre;
 
     /**
      * Ruft den Wert der id-Eigenschaft ab.
      * 
      */
     public int getId() {
         return id;
     }
 
     /**
      * Legt den Wert der id-Eigenschaft fest.
      * 
      */
     public void setId(int value) {
         this.id = value;
     }
 
     /**
      * Ruft den Wert der artist-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getArtist() {
         return artist;
     }
 
     /**
      * Legt den Wert der artist-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setArtist(String value) {
         this.artist = value;
     }
 
     /**
      * Ruft den Wert der title-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Legt den Wert der title-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setTitle(String value) {
         this.title = value;
     }
 
     /**
      * Ruft den Wert der length-Eigenschaft ab.
      * 
      */
     public int getLength() {
         return length;
     }
 
     /**
      * Legt den Wert der length-Eigenschaft fest.
      * 
      */
     public void setLength(int value) {
         this.length = value;
     }
 
     /**
      * Ruft den Wert der album-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getAlbum() {
         return album;
     }
 
     /**
      * Legt den Wert der album-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setAlbum(String value) {
         this.album = value;
     }
 
     /**
      * Ruft den Wert der albumCover-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getAlbumCover() {
         return albumCover;
     }
 
     /**
      * Legt den Wert der albumCover-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setAlbumCover(String value) {
         this.albumCover = value;
     }
 
     /**
      * Ruft den Wert der albumArtist-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getAlbumArtist() {
         return albumArtist;
     }
 
     /**
      * Legt den Wert der albumArtist-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setAlbumArtist(String value) {
         this.albumArtist = value;
     }
 
     /**
      * Ruft den Wert der numberInAlbum-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link Integer }
      *     
      */
     public Integer getNumberInAlbum() {
         return numberInAlbum;
     }
 
     /**
      * Legt den Wert der numberInAlbum-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link Integer }
      *     
      */
     public void setNumberInAlbum(Integer value) {
         this.numberInAlbum = value;
     }
 
     /**
      * Ruft den Wert der genre-Eigenschaft ab.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
     public String getGenre() {
         return genre;
     }
 
     /**
      * Legt den Wert der genre-Eigenschaft fest.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
     public void setGenre(String value) {
         this.genre = value;
     }
 
 }
