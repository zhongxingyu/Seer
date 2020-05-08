 package org.malai.instrument.library;
 
 import java.awt.event.KeyEvent;
 import java.util.List;
 
 import org.malai.action.library.Zoom;
 import org.malai.error.ErrorCatcher;
 import org.malai.instrument.Instrument;
 import org.malai.instrument.Link;
 import org.malai.interaction.library.KeysScrolling;
 import org.malai.properties.Zoomable;
 
 /**
  * This instrument allows to zoom on the canvas.<br>
  * <br>
  * This file is part of Malai.<br>
  * Copyright (c) 2009-2012 Arnaud BLOUIN<br>
  * <br>
  * Malai is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later version.
  * <br>
  * Malai is distributed without any warranty; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.<br>
  * <br>
  * 2012-05-25<br>
  * @author Arnaud BLOUIN
  * @version 0.2
  * @since 0.2
  */
 public class BasicZoomer extends Instrument {
 	/** The object to zoom in/out. */
 	protected Zoomable zoomable;
 
 
 	/**
 	 * Creates and initialises the zoomer.
 	 * @param zoomable The zoomable object to zoom in/out.
 	 * @throws IllegalArgumentException If the given canvas is null;
 	 * @since 3.0
 	 */
 	public BasicZoomer(final Zoomable zoomable) {
 		super();
 
 		if(zoomable==null)
 			throw new IllegalArgumentException();
 
 		this.zoomable = zoomable;
 	}
 
 
 	/**
 	 * @return The object to zoom in/out.
 	 */
 	public Zoomable getZoomable() {
 		return zoomable;
 	}
 
 
 	@Override
 	protected void initialiseLinks() {
 		try{
 			addLink(new Scroll2Zoom(this));
 		}catch(final InstantiationException e){
 			ErrorCatcher.INSTANCE.reportError(e);
 		}catch(final IllegalAccessException e){
 			ErrorCatcher.INSTANCE.reportError(e);
 		}
 	}
 
 
 	/**
 	 * This link maps a scroll interaction to a zoom action.
 	 */
 	protected class Scroll2Zoom extends Link<Zoom, KeysScrolling, BasicZoomer> {
 		/**
 		 * Creates the action.
 		 */
 		protected Scroll2Zoom(final BasicZoomer ins) throws InstantiationException, IllegalAccessException {
 			super(ins, false, Zoom.class, KeysScrolling.class);
 		}
 
 		@Override
 		public void initAction() {
 			action.setZoomable(instrument.zoomable);
 		}
 
 		@Override
 		public void updateAction() {
 			action.setZoomLevel(instrument.zoomable.getZoom() + (interaction.getIncrement()>0 ? Zoomable.ZOOM_INCREMENT : -Zoomable.ZOOM_INCREMENT));
 			action.setPx(interaction.getPx());
			action.setPy(interaction.getPx());
 		}
 
 		@Override
 		public boolean isConditionRespected() {
 			final List<Integer> keys = interaction.getKeys();
 			return keys.size()==1 && keys.get(0)==KeyEvent.VK_CONTROL;
 		}
 	}
 }
