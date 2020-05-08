 package arithmetic.objects.groups;
 
 
 
 import java.io.UnsupportedEncodingException;
 import arithmetic.objects.LargeInteger;
 
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.field.IField;
 import arithmetic.objects.field.IntegerFieldElement;
 import arithmetic.objects.field.PrimeOrderField;
 import arithmetic.objects.field.PrimeOrderFieldTests;
 
 
 
 public class ECurveGroupElement implements IGroupElement {
 
 	private Point element;
 	private ECurveGroup group;
 
 	public ECurveGroupElement(Point element, ECurveGroup group) {
 		this.element = element;
 		this.group = group;
 	}
 
 
 	public Point getElement() {
 		return element;
 	}
 
 	public ECurveGroup getGroup() {
 		return group;
 	}
 
 	@Override
 	public ECurveGroupElement mult(IGroupElement b) {
 		if (b.equal(group.one())) {
 			return this;
 		}
 			
 		IntegerFieldElement xp = this.getElement().getX();
 		IntegerFieldElement xq = ((ECurveGroupElement) b).getElement().getX();
 		IntegerFieldElement yp = this.getElement().getY(); 
 		IntegerFieldElement yq = ((ECurveGroupElement) b).getElement().getY();
 			
 		if (!xp.equal(xq)) {
			IntegerFieldElement s = (yp.subtract(yq)).divide(xp.subtract(xq));
 			IntegerFieldElement xr = s.power(new LargeInteger("2")).subtract(xp).subtract(xq);
			IntegerFieldElement yr = LargeInteger.ZERO.subtract(yp.add(s.multiply(xr.subtract(xp))));
 			ECurveGroupElement ret = new ECurveGroupElement(new Point(xr, yr), getGroup());
 			return ret;
 		}
 		
 		if (yp.equal(yq.neg())) {
 			return group.one();
 		}
 		
 		if (yp.equal(yq)) {
			IField<IntegerFieldElement> field = new PrimeOrderField(this.getGroup().getFieldOrder());
			IntegerFieldElement s = (xp.power(new LargeInteger("2")).mult(new IntegerFieldElement(new LargeInteger("3"),field)).subtract(this.getGroup().getXCoefficient()));
					
			LargeInteger s = getElement().getX().getElement().power(2).multiply(new LargeInteger("3")).subtract(group.getXCoefficient()).divide(getElement().getY().getElement().multiply(new LargeInteger("2")));
			LargeInteger x = s.power(2).subtract(getElement().getX().getElement().multiply(new LargeInteger("2")));
			LargeInteger y = getElement().getY().getElement().add(s.multiply(x.subtract(getElement().getX().getElement())));
			IntegerFieldElement newY = new IntegerFieldElement(y, getElement().getY().getField());
			IntegerFieldElement newX = new IntegerFieldElement(x, getElement().getX().getField());
 			Point p = new Point(newX, newY);
 			ECurveGroupElement ret = new ECurveGroupElement(p, getGroup());
 			return ret;
 		}
 
 		System.out.println("Error.");
 		return null;
 	}
 
 
 	@Override
 	public ECurveGroupElement inverse() {
 		IntegerFieldElement y = new IntegerFieldElement(LargeInteger.ZERO.subtract(((Point) getElement()).getY().getElement()).mod(getGroup().getFieldOrder()), null);
 		Point p = new Point(getElement().getX(), y);
 		ECurveGroupElement ret = new ECurveGroupElement(p, getGroup());
 		return ret;
 	}
 
 	@Override
 	public ECurveGroupElement divide(IGroupElement b) {
 		return mult(b.inverse());
 	}
 
 	public ECurveGroupElement power(LargeInteger b) {
 		ECurveGroupElement result = this;
 		for (LargeInteger i = LargeInteger.ONE; i.compareTo(b)<0; i = i.add(LargeInteger.ONE))
 			result = result.mult(this);
 		return result;
 	}
 
 
 	@Override
 	public boolean equal(IGroupElement b) {
 		if (getElement().getX().equal((((ECurveGroupElement) b).getElement()).getX()) && (getElement()).getY().equal((((ECurveGroupElement) b).getElement()).getY()))
 			return true;
 		else return false;
 	}
 
 	@Override
 	public byte[] toByteArray() throws UnsupportedEncodingException {
 		Node pointNode = new Node();
 		pointNode.add(element.getX());
 		pointNode.add(element.getY());
 		return pointNode.toByteArray();
 	}
 
 
 }
