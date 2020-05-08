 package org.alahijani.lf.parser;
 
 import com.intellij.lang.PsiBuilder;
 import com.intellij.psi.tree.IElementType;
 import org.alahijani.lf.TwelfBundle;
 import org.alahijani.lf.lang.TwelfConfigTokenType;
 import org.alahijani.lf.lang.TwelfElementType;
 
 /**
  * @author Ali Lahijani
  */
 public class TwelfConfigParsing {
     private PsiBuilder builder;
 
     public TwelfConfigParsing(PsiBuilder builder) {
         this.builder = builder;
     }
 
     public boolean document() {
         while (!builder.eof()) {
             filename();
         }
         return true;
     }
 
     private void filename() {
         PsiBuilder.Marker marker = builder.mark();
         if (builder.getTokenType() == TwelfConfigTokenType.FILENAME) {
             eatElement(TwelfElementType.FILE_NAME);
             marker.done(TwelfElementType.TWELF_FILE_REFERENCE);
         } else {
            String unexpected = builder.getTokenText();
             builder.advanceLexer();
            marker.error(TwelfBundle.message("unexpected.token", unexpected));
         }
     }
 
     private void eatElement(IElementType elementType) {
         PsiBuilder.Marker marker = builder.mark();
         builder.advanceLexer();
         marker.done(elementType);
     }
 }
