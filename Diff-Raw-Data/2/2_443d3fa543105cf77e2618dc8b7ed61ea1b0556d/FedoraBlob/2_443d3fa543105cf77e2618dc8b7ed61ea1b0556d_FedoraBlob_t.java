 package edu.illinois.medusa;
 
 import java.net.URI;
 
 /**
  * Extension of HintedBlob that adds hints apropos to use with Fedora. This includes headers for the stream id,
  * repository name, and MD5 sum as well. Also handles copying headers appropriately.
  *
  * @author Howard Ding - hding2@illinois.edu
  */
 public class FedoraBlob extends HintedBlob {
 
     /**
      * Construct a new FedoraBlob.
      *
      * @param owner Owning connection
      * @param id    ID of blob
      * @param hints Any existing hints to use
      */
     protected FedoraBlob(FedoraBlobStoreConnection owner, URI id, CaringoHints hints) {
         super(owner, id, hints);
         this.hintCopier.addRuleFront(new HintCopyRegexpRule("reject-stream-id", false, "^x-fedora-meta-stream-id$"));
        this.hintCopier.addRuleFront(new HintCopyRegexpRule("reject-repository-name", false, "^x-fedora-meta-repository-name$"));
         this.hintAdders.add(new HintIdAdder("fedora:stream-id"));
         this.hintAdders.add(new HintMD5Adder());
     }
 
     /**
      * Action before writing to storage. We add a header here.
      *
      * @param content The OutputStream with the bytes to be written
      */
     protected void preprocessWrite(CaringoOutputStream content) {
         super.preprocessWrite(content);
         this.addHint(":Cache-Control", "no-cache");
     }
 }
