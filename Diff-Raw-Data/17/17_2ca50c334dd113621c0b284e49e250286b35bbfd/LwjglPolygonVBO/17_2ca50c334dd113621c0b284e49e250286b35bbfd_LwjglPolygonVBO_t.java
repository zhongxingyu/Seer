 /*******************************************************************************
  * Copyright (c) 2009 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kristian Duske - initial API and implementation
  ******************************************************************************/
 package org.eclipse.draw3d.graphics3d.lwjgl.graphics;
 
 import java.nio.IntBuffer;
 
 import org.eclipse.draw3d.graphics.optimizer.PrimitiveSet;
 import org.eclipse.draw3d.graphics.optimizer.classification.PrimitiveClass;
 import org.eclipse.draw3d.graphics.optimizer.primitive.OutlineRenderRule;
 import org.eclipse.draw3d.graphics.optimizer.primitive.PolygonPrimitive;
 import org.eclipse.draw3d.graphics.optimizer.primitive.Primitive;
 import org.eclipse.draw3d.graphics.optimizer.primitive.SolidRenderRule;
 import org.eclipse.draw3d.graphics3d.Graphics3D;
 import org.eclipse.draw3d.graphics3d.lwjgl.Graphics3DLwjgl;
 import org.eclipse.draw3d.util.ColorConverter;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL14;
 
 /**
  * Vertex buffer object that renders polygons.
  * 
  * @author Kristian Duske
  * @version $Revision$
  * @since 21.12.2009
  */
 public class LwjglPolygonVBO extends LwjglVertexPrimitiveVBO {
 
 	private float[] m_color = new float[4];
 
 	private IntBuffer m_indexBuffer;
 
 	private IntBuffer m_countBuffer;
 
 	private int m_vertexCount;
 
 	private boolean m_solid;
 
 	/**
 	 * Creates a new VBO that renders the given polygon primitives.
 	 * 
 	 * @param i_primitives the primitives to render
 	 * @throws NullPointerException if the given primitive set is
 	 *             <code>null</code>
 	 * @throws IllegalArgumentException if the given primitive set is empty or
 	 *             if it does not contain polygons
 	 */
 	public LwjglPolygonVBO(PrimitiveSet i_primitives) {
 
 		super(i_primitives);
 
 		PrimitiveClass clazz = i_primitives.getPrimitiveClass();
 		if (!clazz.isPolygon())
 			throw new IllegalArgumentException(i_primitives
 				+ " does not contain polygons");
 
 		int count = i_primitives.getSize();
 		if (count == 1) {
 			m_vertexCount = i_primitives.getVertexCount();
 		} else {
 			m_indexBuffer = BufferUtils.createIntBuffer(count);
 			m_countBuffer = BufferUtils.createIntBuffer(count);
 
 			int index = 0;
 			for (Primitive primitive : i_primitives.getPrimitives()) {
 				PolygonPrimitive polygon = (PolygonPrimitive) primitive;
 
 				int vertexCount = polygon.getVertexCount();
 				m_countBuffer.put(vertexCount);
 				m_indexBuffer.put(index);
				index += vertexCount;
 			}
 
 			m_indexBuffer.rewind();
 			m_countBuffer.rewind();
 		}
 
 		m_solid = clazz.isSolid();
 		if (m_solid) {
 			SolidRenderRule rule = clazz.getRenderRule().asSolid();
 			ColorConverter.toFloatArray(rule.getColor(), rule.getAlpha(),
 				m_color);
 		} else {
 			OutlineRenderRule rule = clazz.getRenderRule().asOutline();
 			ColorConverter.toFloatArray(rule.getColor(), rule.getAlpha(),
 				m_color);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.graphics3d.lwjgl.graphics.LwjglVBO#dispose()
 	 */
 	@Override
 	public void dispose() {
 
 		m_indexBuffer = null;
 		m_countBuffer = null;
 
 		super.dispose();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.graphics3d.lwjgl.graphics.LwjglVBO#doRender(org.eclipse.draw3d.graphics3d.Graphics3D)
 	 */
 	@Override
 	protected void doRender(Graphics3D i_g3d) {
 
 		i_g3d.glColor4f(m_color);
 
 		if (m_solid) {
 			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
 
 			if ((m_indexBuffer != null && m_countBuffer != null))
 				GL14.glMultiDrawArrays(GL11.GL_POLYGON, m_indexBuffer,
 					m_countBuffer);
 			else
 				GL11.glDrawArrays(GL11.GL_POLYGON, 0, m_vertexCount);
 		} else {
 			GL11.glTranslatef(Graphics3DLwjgl.RASTER_OFFSET,
 				Graphics3DLwjgl.RASTER_OFFSET, 0);
 
 			if ((m_indexBuffer != null && m_countBuffer != null))
 				GL14.glMultiDrawArrays(GL11.GL_LINE_LOOP, m_indexBuffer,
 					m_countBuffer);
 			else
 				GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, m_vertexCount);
 		}
 	}
 }
