 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executer.
  * 
  * Universal Task Executer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executer is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executer. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.gui.nodes;
 
 import net.lmxm.ute.ConfigurationHolder;
 import net.lmxm.ute.beans.Configuration;
 
 /**
  * The Class PropertiesRootTreeNode.
  */
 public final class PropertiesRootTreeNode extends AbstractRootTreeNode {
 
 	/**
 	 * Instantiates a new properties root tree node.
 	 * 
 	 * @param configurationHolder the configuration holder
 	 */
 	public PropertiesRootTreeNode(final ConfigurationHolder configurationHolder) {
 		super(configurationHolder);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.lmxm.ute.gui.nodes.AbstractRootTreeNode#convertToString(net.lmxm.ute.beans.Configuration)
 	 */
 	@Override
 	protected String convertToString(final Configuration configuration) {
		System.out.println("Properties (" + configuration.getFileSystemLocations().size() + ")");
		return "Properties (" + configuration.getProperties().size() + ")";
 	}
 }
