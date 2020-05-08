 class MouseWorld extends PhysicsWorld {
 	public MouseWorld() {
 		super(640, 480);
 		Ball b = new Ball(0, 0);
 		addObject(b, 320, 240);
		b.acceptMouse = true;
 	}
 }
