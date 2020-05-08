 import html2windows.css.CSSParser;
 import html2windows.css.FontPainter;
 import html2windows.dom.Document;
 import html2windows.dom.UIParser;
 
 
 public class simple {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Document document = new UIParser().parse("<html><head>simple</head><body><h1>Hello World</h1></body></html>");
        document.setPainter(new FontPainter());
         document.setSize(400, 300);
         new CSSParser().parse("h1 { font-color: black; }", document);
         document.setVisible(true);
     }
 }
