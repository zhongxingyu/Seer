 package ca.cmput301f13t03.adventure_datetime.view.treeView;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 
 class Camera
 {
 	// these will be overwritten early on
 	private static final float DEFAULT_WIDTH = 500;
 	private static final float DEFAULT_HEIGHT = 1000;
 	
 	// x, y position of the top left of the screen in world coordinates
 	private float x = 0;
 	private float y = 0;
 	
 	// size of the view
 	private float m_viewWidth = DEFAULT_WIDTH;
 	private float m_viewHeight = DEFAULT_HEIGHT;
 	
 	// coords we want to keep centered
 	private float m_xTarget = 0 - m_viewWidth / 2;
 	private float m_yTarget = 0 - m_viewWidth / 2;
 	
 	private float m_zoomLevel = 1.0f;
 	
 	private Matrix m_transform = null;
 	private Matrix m_inverse = null;
 	
 	private Object m_syncLock = new Object();
 	
 	public Camera()
 	{
 		
 	}
 	
 	public void DrawLocal(Canvas canvas, Paint paint, Path p)
 	{
 		p.transform(this.GetTransform());
 		canvas.drawPath(p, paint);
 		p.transform(this.GetInverseTransform());
 	}
 	
 	public void DrawLocal(Canvas canvas, Paint paint, Bitmap image, int x, int y)
 	{
 		Matrix trans = new Matrix(GetTransform());
 		trans.setTranslate(x - this.x, y - this.y);
 		canvas.drawBitmap(image, trans, paint);
 	}
 	
 	public void DrawLocal(Canvas canvas, Paint paint, String text, int centerX, int centerY)
 	{
 		float points[] = { centerX, centerY };
 		
 		this.GetTransform().mapPoints(points);
 		
 		canvas.drawText(text, points[0], points[1], paint);
 	}
 	
 	private Matrix GetTransform()
 	{
 		Matrix result = null;
 		
 		synchronized (m_syncLock) 
 		{
 			if(m_transform == null)
 			{
 				m_transform = new Matrix();
 				m_transform.setScale(m_zoomLevel, m_zoomLevel);
 				m_transform.setTranslate(-this.x, -this.y);
 			}
 			
 			result = m_transform;
 		}
 		
 		return result;
 	}
 	
 	private Matrix GetInverseTransform()
 	{
 		Matrix result = null;
 		
 		synchronized (m_syncLock) 
 		{
 			if(m_inverse == null)
 			{
 				m_inverse = new Matrix();
 				m_inverse.setScale(1.0f / m_zoomLevel, 1.0f / m_zoomLevel);
 				m_inverse.setTranslate(this.x, this.y);
 			}
 			
 			result = m_inverse;
 		}
 		
 		return result;
 	}
 	
 	public float GetXTarget()
 	{
 		return m_xTarget;
 	}
 	
 	public float GetYTarget()
 	{
 		return m_yTarget;
 	}
 	
 	public void ResizeView(float width, float height)
 	{
 		synchronized (m_syncLock) 
 		{
 			m_viewWidth = width;
 			m_viewHeight = height;
			
			m_inverse = null;
			m_transform = null;
 		}
 		
 		LookAt(m_xTarget, m_yTarget);
 	}
 	
 	public void LookAt(float x, float y)
 	{
 		synchronized (m_syncLock) 
 		{
 			m_xTarget = x;
 			m_yTarget = y;
 			
 			this.x = m_xTarget - m_viewWidth / 2;
 			this.y = m_yTarget - m_viewHeight / 2;
 			
 			m_inverse = null;
 			m_transform = null;
 		}
 	}
 	
 	public void ScreenCordsToWorldCords(float[] screenCords)
 	{
 		GetInverseTransform().mapPoints(screenCords);
 	}
 }
