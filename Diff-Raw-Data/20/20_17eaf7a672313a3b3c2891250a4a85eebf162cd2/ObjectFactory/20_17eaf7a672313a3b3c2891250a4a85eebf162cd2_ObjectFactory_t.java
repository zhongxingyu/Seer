 //
 // Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
 // Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
 // nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.06.23 um 03:12:10 PM CEST 
 //
 
 
 package djproject.songs;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.XmlElementDecl;
 import javax.xml.bind.annotation.XmlRegistry;
 import javax.xml.namespace.QName;
 
 
 /**
  * This object contains factory methods for each 
  * Java content interface and Java element interface 
  * generated in the djproject.songs package. 
  * <p>An ObjectFactory allows you to programatically 
  * construct new instances of the Java representation 
  * for XML content. The Java representation of XML 
  * content can consist of schema derived interfaces 
  * and classes representing the binding of schema 
  * type definitions, element declarations and model 
  * groups.  Factory methods for each of these are 
  * provided in this class.
  * 
  */
 @XmlRegistry
 public class ObjectFactory {
 
     private final static QName _Id_QNAME = new QName("", "id");
     private final static QName _AlbumArtist_QNAME = new QName("", "album_artist");
     private final static QName _Genre_QNAME = new QName("", "genre");
     private final static QName _Title_QNAME = new QName("", "title");
     private final static QName _Album_QNAME = new QName("", "album");
     private final static QName _AlbumCover_QNAME = new QName("", "album_cover");
     private final static QName _Length_QNAME = new QName("", "length");
     private final static QName _Artist_QNAME = new QName("", "artist");
     private final static QName _NumberInAlbum_QNAME = new QName("", "number_in_album");
 
     /**
      * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: djproject.songs
      * 
      */
     public ObjectFactory() {
     }
 
     /**
      * Create an instance of {@link Song }
      * 
      */
     public Song createSong() {
         return new Song();
     }
 
     /**
      * Create an instance of {@link Songs }
      * 
      */
     public Songs createSongs() {
         return new Songs();
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "id")
     public JAXBElement<Integer> createId(Integer value) {
         return new JAXBElement<Integer>(_Id_QNAME, Integer.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "album_artist")
     public JAXBElement<String> createAlbumArtist(String value) {
         return new JAXBElement<String>(_AlbumArtist_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "genre")
     public JAXBElement<String> createGenre(String value) {
         return new JAXBElement<String>(_Genre_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "title")
     public JAXBElement<String> createTitle(String value) {
         return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "album")
     public JAXBElement<String> createAlbum(String value) {
         return new JAXBElement<String>(_Album_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "album_cover")
     public JAXBElement<String> createAlbumCover(String value) {
         return new JAXBElement<String>(_AlbumCover_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "length")
     public JAXBElement<Integer> createLength(Integer value) {
         return new JAXBElement<Integer>(_Length_QNAME, Integer.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "artist")
     public JAXBElement<String> createArtist(String value) {
         return new JAXBElement<String>(_Artist_QNAME, String.class, null, value);
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "", name = "number_in_album")
     public JAXBElement<Integer> createNumberInAlbum(Integer value) {
         return new JAXBElement<Integer>(_NumberInAlbum_QNAME, Integer.class, null, value);
     }
 
 }
