 package com.dc2f.datastore;
 
 public interface WritableContentRepository extends ContentRepository {
 
 	/**
 	 * Write a node to the ContentRepository
 	 * @param node - {@link Node} to write into the ContentRepository
	 * @return <code>true</code> if the node was written to the ContentRepository
 	 */
	boolean writeNode(Node node);
 	
 	/**
 	 * execute a rollback of the transaction so nothing is actually written to the filesystem.
 	 */
 	void rollback();
 }
