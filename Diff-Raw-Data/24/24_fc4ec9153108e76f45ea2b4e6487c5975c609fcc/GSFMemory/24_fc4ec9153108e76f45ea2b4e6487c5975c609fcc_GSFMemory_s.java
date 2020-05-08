 package wordlists;
 
 import java.lang.Character;
 import java.lang.Integer;
 
 import java.util.TreeMap;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.Lock;
 import java.text.Collator;
 
 import java.io.RandomAccessFile;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 
 import java.io.IOException;
 import wordlists.exceptions.*;
 
 import utils.Word;
 import utils.CharacterCollator;
 
 /**
  * Class implementing word list working with GSF and DWA files, which 
  * before working it loads into memory.
  *
  * Note that before word list can be used, DWA or GSF file must be loaded
  * by calling method load.
  */
 public class GSFMemory extends WordList implements 
     IWordListChange, IWordListFileWrite, IWordListFileRead {
 
   private Node root = new Node();
   private String filename = null;
 
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final Lock readLock = lock.readLock();
   private final Lock writeLock = lock.writeLock();
 
   /**
    * Internal class representing single trie node.
    */
   class Node {
 
     private String definition = null;
     private TreeMap<Character, Node> next = null;
     private Integer address = null;
 
     /**
      * Creates empty node.
      */
     public Node () {
       CharacterCollator collator = CharacterCollator.getInstance();
                                         // FIXME: Collator must be WordList
                                         // dependent, not program dependent.
       next = new TreeMap<Character, Node>(collator);
       }
 
     /**
      * Returns Node, which is represented by given symbol. 
      *
      * If node doesn't exists throws an exception.
      *
      * @param symbol – node identifier.
      * @return Node specified by symbol.
      */
     public Node getNextNode(Character symbol) 
         throws NodeNotExistsException {
 
       if (!this.next.containsKey(symbol)) {
         throw new NodeNotExistsException(
             "Node referenced by \'" + symbol + "\' doesn't exist.");
         }
 
       return this.next.get(symbol);
       }
     
     /** 
      * Returns Node, which is represented by given symbol. 
      *
      * If node doesn't exists, then creates it.
      *
      * @param symbol – node identifier.
      * @return Node specified by symbol.
      */
     public Node getCreateNextNode(Character symbol) {
 
       if (!this.next.containsKey(symbol)) {
         Node node = new Node();
         this.next.put(symbol, node);
         }
 
       return this.next.get(symbol);
       }
 
     /**
      * Assigns passed node as this node child.
      *
      * @param symbol – node identifier.
      * @param node – node to assign.
      */
     public void assignNextNode(Character symbol, Node node) 
         throws DuplicateNodeException {
 
       if (this.next.containsKey(symbol)) {
         throw new DuplicateNodeException("Node already assigned!");
         }
 
       this.next.put(symbol, node);
 
       return;
       }
 
     /**
      * Returns word definition.
      *
      * @return word definition or null if isWord is false.
      */
     public String getDefinition() {
       return this.definition;
       }
     
     /**
      * Assigns word definition to node.
      *
      * @param definition – word definition to assign.
      */
     public void setDefinition(String definition) {
       this.definition = definition;
       }
     
     /**
      * Returns if this node represents word.
      *
      * @return true if represents, false – otherwise.
      */
     public boolean isWord() {
       return this.definition != null;
       }
 
     /**
      * Returns symbols with which other trie nodes could be reached from
      * this node.
      *
      * @return list of letters, which could be used for getNextNode.
      */
     public LinkedList<Character> getLettersList() {
 
       Set<Character> keySet = this.next.keySet();
       LinkedList<Character> keys = new LinkedList<Character>();
 
       for (Character key : keySet)
         keys.add(key);
 
       return keys;
       }
     
     /**
      * Returns address of first byte of this node representation in file.
      *
      * @return address or null if unknown.
      */
     public Integer getAddress() {
       return this.address;
       }
     
     /**
      * Assigns address of first byte of this node representation if file.
      *
      * @param address.
      */
     public void setAddress(Integer address) {
       this.address = address;
       }
     
     }
 
   /**
    * Write node to GSF file.
    *
    * @param node – node to write.
    * @param out – file object to which write node.
    */
   private void writeGSF(Node node, RandomAccessFile out) 
       throws IOException, DamagedWordListException {
 
     try {
       for (Character l : node.getLettersList()) {
         this.writeGSF(node.getNextNode(l), out);
         }
       }
     catch (NodeNotExistsException e) {
       throw new DamagedWordListException(
           "NodeNotExistsException in writeGSF was cought.");
       }
 
     node.setAddress(new Integer((int) out.getFilePointer()));
                                         // TODO: Check if returned offset is
                                         // of byte which is going to be 
                                         // written. Not which was written.
     
     for (Character l : node.getLettersList()) {
       out.writeChar(l.charValue());
       out.writeInt(node.getNextNode(l).getAddress());
       }
     
     if (node.isWord()) {
       out.writeChar(1);
       out.writeUTF(node.getDefinition());
       }
     else {
       out.writeChar(0);
       }
 
     return;
     }
 
   /**
    * Save word list to GSF file.
    *
    * @param filename – path to file to which save the word list.
    */
   private void saveGSF(String filename) 
       throws IOException, DamagedWordListException {
     RandomAccessFile out = null;
 
     try {
       out = new RandomAccessFile(filename, "rw");
 
       out.writeBytes("GSF");
       out.writeInt(0);                  // Placeholder for root node 
                                         // address.
       this.writeGSF(this.root, out);    // Dump all nodes.
       out.seek(3);
       out.writeInt(this.root.getAddress());
       }
     finally {
       if (out != null)
         out.close();
       }
     
     this.filename = filename;
 
     return;
     }
 
   /**
    * Write node to DWA file.
    *
    * @param node – node to write.
    * @param out – buffer to which write node.
    * @param word – word which is defined by this node.
    */
   private void writeDWA(
       Node node, BufferedWriter out, String word) 
       throws IOException, DamagedWordListException {
 
     if (node.isWord()) {
      out.write(word+"="+node.getDefinition());
       out.newLine();
       }
     
     try {
       for (Character l : node.getLettersList()) {
         this.writeDWA(node.getNextNode(l), out, word+l.toString());
         }
       }
     catch (NodeNotExistsException e) {
       throw new DamagedWordListException(
           "NodeNotExistsException in writeDWA was cought.");
       }
 
     return;
     }
 
   /**
    * Save word list to DWA file.
    *
    * @param filename – path to file to which save the word list.
    */
   private void saveDWA(String filename) 
       throws IOException, DamagedWordListException {
 
     BufferedWriter out = null;
 
     try {
 
       out = new BufferedWriter( 
           new OutputStreamWriter( 
             new FileOutputStream(filename),"UTF8"));
       
       this.writeDWA(this.root, out, "");
 
       }
     finally {
       if (out != null)
         out.close();
       }
 
     this.filename = filename;
 
     return;
     }
 
   /**
    * Look at IWordListFileWrite interface documentation.
    *
    * File type is specified by extension. ".gsf" for GSF files and ".dwa"
    * for DWA files.
    */
   public void save(String filename) 
       throws IOException, DamagedWordListException {
 
     this.writeLock.lock();
     try {
       if (filename.endsWith(".dwa")) {
         this.saveDWA(filename);
         }
       else if (filename.endsWith(".gsf")) {
         this.saveGSF(filename);
         }
       else {
         throw new IOException("Not supported file type.");
         }
       }
     finally {
       this.writeLock.unlock();
       }
     
     return;
     }
 
   /**
    * Recursively search for a word.
    * @param node – node, which is now being processed.
    * @param request – word to search for.
    * @param index – index of letter, which is being processed.
    * @param left – how many definitions left to find.
    * @param result – list, to which result will be added.
    * @param word – word which was found.
    * @return how many words were added to result.
    */
   private int search(
       Node node, String request, int index, int left, 
       LinkedList<Word> result, String word) throws NodeNotExistsException {
     // TODO: Cleanup this function code!
 
     int added = 0;
 
     if (request.length() > index) {
       Character letter = new Character(request.charAt(index));
 
       CharacterCollator collator = CharacterCollator.getInstance();
 
       for (Character l : node.getLettersList()) {
         if (collator.compare(letter, l) == 0) {
           int addedNow = this.search(node.getNextNode(l), request, index+1, 
               left, result, word+l.toString());
           added += addedNow;
           left -= addedNow;
           if (left <= 0) {
             return added;
             }
           }
         if (collator.compare(letter, l) < 0) {
           int addedNow = this.search(node.getNextNode(l), "", index+1, 
               left, result, word+l.toString());
           added += addedNow;
           left -= addedNow;
           if (left <= 0) {
             return added;
             }
           }
         }
       
       }
     else {
       if (node.isWord()) {
         result.add(new Word(word, node.getDefinition()));
         added++;
         left--;
         if (left <= 0) {
           return added;
           }
         }
       for (Character l : node.getLettersList()) {
         int addedNow = this.search(node.getNextNode(l), request, index+1, 
             left, result, word+l.toString());
         added += addedNow;
         left -= addedNow;
         if (left <= 0) {
           return added;
           }
         }
       }
 
     return added;
     }
 
   /**
    * Look at WordList class documentation.
    */
   public LinkedList<Word> search(String word, int count) 
       throws DamagedWordListException, InvalidCountException {
 
     if (count < 1) {
       throw new InvalidCountException(
           "Count must be possitive number. " + count + " was passed.");
       }
 
     LinkedList<Word> result = new LinkedList<Word>();
 
     this.readLock.lock();
     try {
       search(this.root, word, 0, count, result, "");
       }
     catch (NodeNotExistsException e) {
       throw new DamagedWordListException(
           "NodeNotExistsException in writeDWA was cought.");
       }
     finally {
       this.readLock.unlock();
       }
 
     return result;
     }
 
   /**
    * Look at IWordListFileChange interface documentation.
    */
   public void addWord(String word, String definition) 
       throws InvalidIdentifierException, InvalidDefinitionException, 
         DuplicateIdentifierException {
 
     Node node = this.root;
     
     Collator collator = Collator.getInstance();
                                         // FIXME: Collator must be WordList
                                         // dependent, not program dependent.
     if (collator.compare(word.trim(), "") == 0) {
       throw new InvalidIdentifierException(
           "Identifier can not be empty.");
       }
     if (collator.compare(definition.trim(), "") == 0) {
       throw new InvalidDefinitionException(
           "Definition can not be empty.");
       }
 
     this.writeLock.lock();
     try {
       for (char c : word.toCharArray()) {
         Character letter = new Character(c);
         node = node.getCreateNextNode(letter);
         }
 
       if (node.isWord()) {
         throw new DuplicateIdentifierException(
             "Word " + word + " already exists!");
         }
       else {
         node.setDefinition(definition);
         }
       }
     finally {
       this.writeLock.unlock();
       }
     
     return;
     }
 
   /**
    * Look at IWordListFileChange interface documentation.
    */
   public void updateWord(String word, String definition) 
     throws InvalidDefinitionException, IdentifierNotExistsException,
            DamagedWordListException {
 
     Collator collator = Collator.getInstance();
                                         // FIXME: Collator must be WordList
                                         // dependent, not program dependent.
     if (collator.compare(definition.trim(), "") == 0) {
       throw new InvalidDefinitionException(
           "Definition can not be empty.");
       }
 
     this.writeLock.lock();
     try {
       Node node = this.root;
 
       for (char c : word.toCharArray()) {
         Character letter = new Character(c);
         node = node.getNextNode(letter);
         }
       
       if (node.isWord()) {
         node.setDefinition(definition);
         }
       else {
         throw new IdentifierNotExistsException("Word not found!");
         }
 
       }
     catch (NodeNotExistsException e) {
      throw new DamagedWordListException(
          "NodeNotExistsException in updateWord was cought.");
       }
     finally {
       this.writeLock.unlock();
       }
 
     return;
     }
 
   /**
    * Look at IWordListFileChange interface documentation.
    */
   public void eraseWord(String word) throws IdentifierNotExistsException {
 
     this.writeLock.lock();
     try {
       Node node = this.root;
 
       for (char c : word.toCharArray()) {
         Character letter = new Character(c);
         node = node.getNextNode(letter);  // FIXME: Make normal exceptions.
         }
       
       if (node.isWord()) {
         node.setDefinition(null);         // FIXME: Make normal node erase.
         }
       else {
         throw new IdentifierNotExistsException("Word not found!");
         }
       }
     catch (NodeNotExistsException e) {
       throw new IdentifierNotExistsException("Word not found!");
       }
     finally {
       this.writeLock.unlock();
       }
 
     return;
     }
 
   /**
    * Look at IWordListFileChange interface documentation.
    */
   public String getWordDefinition(String word) 
       throws IdentifierNotExistsException {
 
     this.readLock.lock();
     try {
       Node node = this.root;
 
       for (char c : word.toCharArray()) {
         Character letter = new Character(c);
         node = node.getNextNode(letter);
         }
       
       if (!node.isWord()) {
         throw new IdentifierNotExistsException("Word not found!");
         }
 
       return node.getDefinition();
       }
     catch (NodeNotExistsException e) {
       throw new IdentifierNotExistsException("Word not found!");
       }
     finally {
       this.readLock.unlock();
       }
     }
 
 
   /**
    * Look at IWordListFileRead interface documentation.
    *
    * File type is specified by extension. ".gsf" for GSF files and ".dwa"
    * for DWA files.
    */
   public void load(String filename) 
       throws IOException, WrongFileFormatException {
 
     this.writeLock.lock();
     try {
       this.filename = filename;
 
       if (this.filename.endsWith(".dwa")) {
         this.loadDWA();
         }
       else if (this.filename.endsWith(".gsf")) {
         this.loadGSF();
         }
       else {
         throw new IOException("Unknown file type!");
         }
       }
     finally {
       this.writeLock.unlock();
       }
     
     return;
     }
 
   /**
    * Loads word list from GSF file.
    */
   private void loadGSF() throws IOException, WrongFileFormatException {
 
     RandomAccessFile in = null;
 
     try {
       in = new RandomAccessFile(this.filename, "r");
 
       this.root = new Node();
 
       in.seek(3);                       // Skip "GSF".
       Integer rootAddress = new Integer(in.readInt());
 
       TreeMap<Integer, Node> nodes = new TreeMap<Integer, Node>();
 
       try {
         while (true) {
           Node node = new Node();
 
           node.setAddress(new Integer((int) in.getFilePointer()));
 
           while (true) {
             char l = in.readChar();
             if (((int) l) == 0) {
               break;
               }
             else if (((int) l) == 1) {
               String definition = in.readUTF();
               node.setDefinition(definition);
               break;
               }
             Integer address = new Integer(in.readInt());
             if (!nodes.containsKey(address)) {
               throw new WrongFileFormatException("Node is missing!");
               }
             try {
               node.assignNextNode(new Character(l), nodes.get(address));
               }
             catch (DuplicateNodeException e) {
               throw new WrongFileFormatException("Node already read!");
               }
             }
           if (nodes.containsKey(node.getAddress())) {
             throw new WrongFileFormatException("Node already read!");
             }
           nodes.put(node.getAddress(), node);
           }
         }
       catch (java.io.EOFException e) {
         }
 
       this.root = nodes.get(rootAddress);
       }
     finally {
       if (in != null)
         in.close();
       }
     
     return;
     }
 
   /**
    * Loads word list from DWA file.
    */
   private void loadDWA() throws IOException, WrongFileFormatException {
 
     BufferedReader in = null; 
 
     try {
       in = new BufferedReader( 
           new InputStreamReader( 
             new FileInputStream(this.filename), "UTF8"));
 
       this.root = new Node();
 
       while (true) {
         String str = in.readLine();
         if (str == null)
           break;
         int splitPoint = str.indexOf('=');
         String word = str.substring(0, splitPoint);
         String definition = str.substring(splitPoint+1);
         while (true) {
           try {
             this.addWord(word, definition);  
             break;
             }
           catch (DuplicateIdentifierException e) {
             word = Word.increaseWordIdentifier(word);
             }
           catch (InvalidIdentifierException e) {
             throw new WrongFileFormatException("Invaldi word identifier!");
             }
           catch (InvalidDefinitionException e) {
             throw new WrongFileFormatException("Invaldi word identifier!");
             }
           }
 
         }
 
       }
     finally {
       if (in != null)
         in.close();
       }
     
     return;
     }
 
   }
