 package org.muis.base.widget;
 
 import static org.muis.core.LayoutContainer.LAYOUT_ATTR;
 
 import java.awt.Point;
 
 import org.muis.core.MuisConstants;
 import org.muis.core.MuisException;
 import org.muis.core.MuisLayout;
 import org.muis.core.event.*;
 import org.muis.core.layout.SizeGuide;
 import org.muis.core.mgr.MuisState;
 import org.muis.core.model.ModelAttributes;
 import org.muis.core.tags.Template;
 
 /** Implements a button. Buttons can be set to toggle mode or normal mode. Buttons are containers that may have any type of content in them. */
 @Template(location = "../../../../button.muis")
 public class Button extends org.muis.core.MuisTemplate {
 	private boolean theLayoutCallbackLock;
 
 	/** Creates a button */
 	public Button() {
 		setFocusable(true);
 		atts().accept(new Object(), LAYOUT_ATTR);
 		atts().accept(new Object(), ModelAttributes.action);
 		life().runWhen(new Runnable() {
 			@Override
 			public void run() {
				state().addListener(MuisConstants.States.CLICK_NAME, new org.muis.core.mgr.StateEngine.StateListener() {
 					private Point theClickLocation;
 
 					@Override
 					public void entered(MuisState state, MuisEvent<?> cause) {
 						if(cause instanceof MouseEvent) {
 							theClickLocation = ((MouseEvent) cause).getPosition(Button.this);
 						} else
 							theClickLocation = null;
 					}
 
 					@Override
 					public void exited(MuisState state, MuisEvent<?> cause) {
 						if(theClickLocation == null || !(cause instanceof MouseEvent))
 							return;
 						Point click = theClickLocation;
 						theClickLocation = null;
 						Point unclick = ((MouseEvent) cause).getPosition(Button.this);
 						int dx = click.x - unclick.x;
 						int dy = click.y - unclick.y;
 						double tol = Button.this.getStyle().getSelf().get(org.muis.base.style.ButtonStyles.clickTolerance);
 						if(dx > tol || dy > tol)
 							return;
 						double dist2 = dx * dx + dy * dy;
 						if(dist2 > tol * tol)
 							return;
 						org.muis.core.model.MuisActionListener listener = atts().get(ModelAttributes.action);
 						if(listener == null)
 							return;
 						try {
 							listener.actionPerformed((MouseEvent) cause);
 						} catch(RuntimeException e) {
 							msg().error("Action listener threw exception", e);
 						}
 					}
 				});
 				addListener(MuisConstants.Events.ATTRIBUTE_CHANGED, new AttributeChangedListener<MuisLayout>(LAYOUT_ATTR) {
 					@Override
 					public void attributeChanged(AttributeChangedEvent<MuisLayout> event) {
 						if(theLayoutCallbackLock)
 							return;
 						theLayoutCallbackLock = true;
 						try {
 							getContentPane().atts().set(LAYOUT_ATTR, event.getValue());
 						} catch(MuisException e) {
 							throw new IllegalStateException(LAYOUT_ATTR + " not accepted by content pane?", e);
 						} finally {
 							theLayoutCallbackLock = false;
 						}
 					}
 				});
 				MuisLayout layout = atts().get(LAYOUT_ATTR);
 				if(layout != null)
 					try {
 						getContentPane().atts().set(LAYOUT_ATTR, layout);
 					} catch(MuisException e) {
 						throw new IllegalStateException(LAYOUT_ATTR + " not accepted by content pane?", e);
 					}
 				getContentPane().addListener(MuisConstants.Events.ATTRIBUTE_CHANGED, new AttributeChangedListener<MuisLayout>(LAYOUT_ATTR) {
 					@Override
 					public void attributeChanged(AttributeChangedEvent<MuisLayout> event) {
 						if(theLayoutCallbackLock)
 							return;
 						theLayoutCallbackLock = true;
 						try {
 							atts().set(LAYOUT_ATTR, event.getValue());
 						} catch(MuisException e) {
 							throw new IllegalStateException(LAYOUT_ATTR + " not accepted by button?", e);
 						} finally {
 							theLayoutCallbackLock = false;
 						}
 					}
 				});
 			}
 		}, MuisConstants.CoreStage.INITIALIZED.toString(), 1);
 	}
 
 	/** @return The panel containing the contents of this button */
 	public Block getContentPane() {
 		return (Block) getElement(getTemplate().getAttachPoint("contents"));
 	}
 
 	@Override
 	public void doLayout() {
 		org.muis.core.style.Size radius = getStyle().getSelf().get(org.muis.core.style.BackgroundStyles.cornerRadius);
 		int w = bounds().getWidth();
 		int h = bounds().getHeight();
 		int lOff = radius.evaluate(w);
 		int tOff = radius.evaluate(h);
 		getContentPane().bounds().setBounds(lOff, tOff, w - lOff - lOff, h - tOff - tOff);
 	}
 
 	@Override
 	public SizeGuide getWSizer() {
 		final org.muis.core.style.Size radius = getStyle().getSelf().get(org.muis.core.style.BackgroundStyles.cornerRadius);
 		return new RadiusAddSizePolicy(getContentPane().getWSizer(), radius);
 	}
 
 	@Override
 	public SizeGuide getHSizer() {
 		final org.muis.core.style.Size radius = getStyle().getSelf().get(org.muis.core.style.BackgroundStyles.cornerRadius);
 		return new RadiusAddSizePolicy(getContentPane().getHSizer(), radius);
 	}
 
 	private static class RadiusAddSizePolicy extends org.muis.core.layout.AbstractSizeGuide {
 		private final SizeGuide theWrapped;
 
 		private org.muis.core.style.Size theRadius;
 
 		RadiusAddSizePolicy(SizeGuide wrap, org.muis.core.style.Size rad) {
 			theWrapped = wrap;
 			theRadius = rad;
 		}
 
 		@Override
 		public int getMinPreferred(int crossSize, boolean csMax) {
 			return addRadius(theWrapped.getMinPreferred(crossSize, csMax));
 		}
 
 		@Override
 		public int getMaxPreferred(int crossSize, boolean csMax) {
 			return addRadius(theWrapped.getMaxPreferred(crossSize, csMax));
 		}
 
 		@Override
 		public int getMin(int crossSize, boolean csMax) {
 			return addRadius(theWrapped.getMin(removeRadius(crossSize), csMax));
 		}
 
 		@Override
 		public int getPreferred(int crossSize, boolean csMax) {
 			return addRadius(theWrapped.getPreferred(removeRadius(crossSize), csMax));
 		}
 
 		@Override
 		public int getMax(int crossSize, boolean csMax) {
 			return addRadius(theWrapped.getMax(removeRadius(crossSize), csMax));
 		}
 
 		@Override
 		public int getBaseline(int size) {
 			int remove = size - removeRadius(size);
 			int ret = theWrapped.getBaseline(size - remove * 2);
 			return ret + remove;
 		}
 
 		int addRadius(int size) {
 			switch (theRadius.getUnit()) {
 			case pixels:
 			case lexips:
 				size += theRadius.getValue() * 2;
 				break;
 			case percent:
 				float radPercent = theRadius.getValue() * 2;
 				if(radPercent >= 100)
 					radPercent = 90;
 				size = Math.round(size * 100 / (100f - radPercent));
 				break;
 			}
 			if(size < 0)
 				return Integer.MAX_VALUE;
 			return size;
 		}
 
 		int removeRadius(int size) {
 			return size - theRadius.evaluate(size);
 		}
 	}
 }
