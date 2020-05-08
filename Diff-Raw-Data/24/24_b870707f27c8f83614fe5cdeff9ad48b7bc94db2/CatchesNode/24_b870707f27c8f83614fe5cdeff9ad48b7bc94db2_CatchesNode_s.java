 package util.ast.node;
 
 import java.util.ArrayList;
 
 import back_end.Visitor;
 
 /**
  * A CatchesNode represents a catch statement with an exception and exception
  * type. E.g.
  * 
  * try { ... } catch (NumberFormateException nfe) { catchStatements; }
  * 
  * @author sam
  * 
  */
 public class CatchesNode extends StatementListNode {
 
 	protected IdNode header;
 	protected StatementListNode block;
 	protected CatchesNode next;
 
 	/**
 	 * Create a new CatchesNode.
 	 * 
 	 * @param header
 	 *            - the IdNode that represents the exception being caught. (e.g.
 	 *            catch (NumberFormatException nfe)).
 	 * @param block
 	 *            - the StatementListNode that represents the statements to
 	 *            execute upon catching the exception represented by IdNode.
 	 */
 	public CatchesNode(IdNode header, StatementListNode block) {
 		this(header, block, null);
 	}
 
 	/**
 	 * Create a new CatchesNode.
 	 * 
 	 * @param header
 	 *            - the IdNode that represents the exception being caught. (e.g.
 	 *            catch (NumberFormatException nfe)).
 	 * @param block
 	 *            - the StatementListNode that represents the statements to
 	 *            execute upon catching the exception represented by IdNode.
 	 * @param next
 	 *            - A (possibly empty) CatchesNode that represents another
 	 *            exception to be caught.
 	 */
 	public CatchesNode(IdNode header, StatementListNode block, CatchesNode next) {
 		super(new ArrayList<Node>());
 		this.header = header;
 		this.block = block;
 		this.next = next;
 		this.addChild(header);
 		this.addChild(block);
 		// protect against having nulls in child list:
 		if (this.hasNext())
 			this.addChild(next);
 	}
 
 	/**
 	 * Get the IdNode that represents the exception being caught at this node.
 	 * 
 	 * @return - the IdNode.
 	 */
 	public IdNode getHeader() {
 		return header;
 	}
 
 	/**
 	 * Get the block of statements to execute upon catching the exception at
 	 * this.header.
 	 * 
 	 * @return - A StatementListNode representing the block of statements to
 	 *         execute.
 	 */
 	public StatementListNode getBlock() {
 		return block;
 	}
 
 	/**
 	 * Ask if there is another catch block after the one represented by this.
 	 * 
 	 * @return - true if there is another catch block, false otherwise.
 	 */
 	public boolean hasNext() {
 		return (next == null);
 	}
 
 	/**
 	 * Return the next catch block.
 	 * 
 	 * @return - the CatchesNode that represents the next catch block.
 	 * @throws UnsupportedOperationException - if this.next == null.
 	 */
 	public CatchesNode getNext() {
 		if (next == null) {
 			throw new UnsupportedOperationException("Trying to retrieve "
 					+ this + "'s next CatchesNode, but it doesn't have one!");
 		}
 		return next;
 	}
 
 	@Override
 	public String getName() {
 		return id + "-CatchesNode newscope: " + isNewScope();
 	}
 
 	@Override
 	public void accept(Visitor v) {
 		v.visit(this);
 	}
 
 }
