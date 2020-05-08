 package model.undo;
 
 import model.Item;
 import model.ProductContainer;
 
 /**
  * Encapsulates the reversible action of transferring an Item between ProductContainers.
  * 
  * @author clayton
  * 
  * @invariant getTransferredItem() != null
  */
 public class TransferItem implements Command {
 	private final Item transferredItem;
 	private final ProductContainer source;
 	private final ProductContainer destination;
	private final boolean addedProduct;
 
 	/**
 	 * Constructs a TransferItem command with the given dependencies.
 	 * 
 	 * @param toTransfer
 	 *            Item to transfer
 	 * @param source
 	 *            ProductContainer source
 	 * @param destination
 	 *            ProductContainer destination
 	 * 
 	 * @pre toTransfer != null
 	 * @pre source != null
 	 * @pre destination != null
 	 * @post true
 	 */
 	public TransferItem(Item toTransfer, ProductContainer source, ProductContainer destination) {
 		transferredItem = toTransfer;
 		this.source = source;
 		this.destination = destination;
		addedProduct = !destination.contains(toTransfer.getProduct());
 	}
 
 	/**
 	 * Transfer an Item based on the source and destination provided on construction.
 	 * 
 	 * @pre getTransferredItem() != null
 	 * @post true
 	 */
 	@Override
 	public void execute() {
 		source.moveIntoContainer(transferredItem, destination);
 	}
 
 	/**
 	 * Returns the Item transferred by the execute method of this Command.
 	 * 
 	 * @return the Item transferred by the execute method of this Command.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Item getTransferredItem() {
 		return transferredItem;
 	}
 
 	/**
 	 * Return an item previously transferred by the execute method.
 	 * 
 	 * @pre getTransferredItem() != null
 	 * @post true
 	 */
 	@Override
 	public void undo() {
 		destination.moveIntoContainer(transferredItem, source);
		if (addedProduct)
			destination.remove(transferredItem.getProduct());
 	}
 }
