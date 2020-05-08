 package com.climber;
 
 import com.badlogic.gdx.math.Rectangle;
 
 
 public class Sprite {
 	private float m_x, m_y;
 	private float m_width, m_height;
 	
 	private boolean mb_isFalling = false;
 	private boolean mb_isJumping = false;
 	
 	private float FALLING_SPEED = 0.2f;
 	private final float RUNNING_SPEED = 5.0f;
 	private final float JUMP_HEIGHT = 3.0f;
 	private final float JUMPING_SPEED = 0.4f;
 	private final float SLIDING_SPEED = FALLING_SPEED / 3.0f;
 	private final float FADE_SPEED = 0.1f;
 	
 	private float mf_jumpTop;
 
 	private boolean mb_AllowedMoveRight = true;
 	private boolean mb_AllowedMoveLeft = true;
 	private boolean mb_AllowedMoveUp = true;
 	private boolean mb_AllowedMoveDown = true;
 	private boolean mb_isSliding;
 	private boolean mb_isDying = false;
 	
 	private float mf_alpha = 1.0f;
 	
 	private boolean mb_isSlidingRight = false;
 	private boolean mb_isSlidingLeft = false;
 	
 	public Sprite(float x, float y, float width, float height) {
 		m_x = x;
 		m_y = y;
 		m_width = width;
 		m_height = height;
 	}
 	public Sprite(float x, float y, float width, float height, boolean isFalling) {
 		m_x = x;
 		m_y = y;
 		m_width = width;
 		m_height = height;
 		mb_isFalling = isFalling;
 	}
 
 	public void simulate() {
 		if ( mb_isFalling && !mb_isSliding) {
 			m_y -= FALLING_SPEED;
 		} else if (mb_isSliding ) {
 			m_y -= SLIDING_SPEED;
 		} else if ( mb_isJumping ) {
 			if ( m_y < mf_jumpTop ) {
 				m_y += JUMPING_SPEED;
 			} else {
 				mb_isJumping = false;
 				mb_isFalling = true;
 			}
 		}
 	}
 	
 	public void fadeOut() {
 		if (mf_alpha == 0.0f)
 			return;
 		
 		mf_alpha -= FADE_SPEED;
 		if ( mf_alpha < 0.0f )
 			mf_alpha = 0.0f;
 	}
 	
 	public void jump() {
 		if (!mb_isSliding && (mb_isFalling || mb_isJumping))
 			return;
 		
 		mb_isJumping = true;
 		mb_isSliding = false;
 		mb_isFalling = false;
 		mf_jumpTop = m_y + JUMP_HEIGHT;
 		
 		if ( mb_isSlidingLeft ) {
 			mb_isSlidingLeft = false;
 		} else if ( mb_isSlidingRight ) {
 			mb_isSlidingRight = false;
 		}
 		
 	}
 	
 	public void interruptJump() {
 		mf_jumpTop = m_y;
 		mb_isJumping = false;
 		mb_isFalling = true;
 	}
 	
 	public void moveX(float deltaTime) {
 		m_x += deltaTime * RUNNING_SPEED;
 	}
 	
 	public float getM_x() {
 		return m_x;
 	}
 
 	public void setM_x(float m_x) {
 		this.m_x = m_x;
 	}
 
 	public float getM_y() {
 		return m_y;
 	}
 
 	public void setM_y(float m_y) {
 		this.m_y = m_y;
 	}
 
 	public float getM_width() {
 		return m_width;
 	}
 
 	public void setM_width(float m_width) {
 		this.m_width = m_width;
 	}
 
 	public float getM_height() {
 		return m_height;
 	}
 
 	public void setM_height(float m_height) {
 		this.m_height = m_height;
 	}
 	public void setFalling(boolean b) {
 		mb_isFalling = b;
 		
 	}
 	
 	public void setFallingSpeed(float speed) {
 		FALLING_SPEED = speed;
 	}
 	public boolean isMb_isFalling() {
 		return mb_isFalling;
 	}
 	public void setMb_isFalling(boolean mb_isFalling) {
 		this.mb_isFalling = mb_isFalling;
 	}
 	public boolean isMb_isJumping() {
 		return mb_isJumping;
 	}
 	public void setMb_isJumping(boolean mb_isJumping) {
 		this.mb_isJumping = mb_isJumping;
 	}
 	public boolean isMb_AllowedMoveRight() {
 		return mb_AllowedMoveRight;
 	}
 	public void setMb_AllowedMoveRight(boolean mb_AllowedMoveRight) {
 		this.mb_AllowedMoveRight = mb_AllowedMoveRight;
 	}
 	public boolean isMb_AllowedMoveLeft() {
 		return mb_AllowedMoveLeft;
 	}
 	public void setMb_AllowedMoveLeft(boolean mb_AllowedMoveLeft) {
 		this.mb_AllowedMoveLeft = mb_AllowedMoveLeft;
 	}
 	public boolean isMb_AllowedMoveUp() {
 		return mb_AllowedMoveUp;
 	}
 	public void setMb_AllowedMoveUp(boolean mb_AllowedMoveUp) {
 		this.mb_AllowedMoveUp = mb_AllowedMoveUp;
 	}
 	public boolean isMb_AllowedMoveDown() {
 		return mb_AllowedMoveDown;
 	}
 	public void setMb_AllowedMoveDown(boolean mb_AllowedMoveDown) {
 		this.mb_AllowedMoveDown = mb_AllowedMoveDown;
 	}
 	public void startSliding() {
 		mb_isSliding = true;
 		mb_isJumping = false;
 		
 	}
 	public void moveY(float deltaTime, float speed) {
 		m_y += deltaTime * speed;
 		
 	}
 	
 	public Rectangle getRectangle() {
 		return new Rectangle(m_x, m_y, m_width, m_height);
 	}
 	public boolean isMb_isDying() {
 		return mb_isDying;
 	}
 	public void setMb_isDying(boolean mb_isDying) {
 		this.mb_isDying = mb_isDying;
 	}
 	public float getAlpha() {
 		return mf_alpha;
 	}
 	public float getYMiddle() {
 		return m_y + (m_height/2.0f);
 	}
 	public boolean isSliding() {
 		return mb_isSliding;
 	}
 	public void setSliding(boolean b) {
 		mb_isSliding = b;
 	}
 	
 	public void setSlidingLeft(boolean b) {
 		mb_isSlidingLeft = b;
 	}
 	public void setSlidingRight(boolean b) {
 		mb_isSlidingRight = b;
 	}
 }
