 /*
  * Thibaut Colar Mar 24, 2010
  */
 
 package net.colar.netbeans.fan.test;
 
import net.colar.netbeans.fan.types.FanResolvedListType;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import net.jot.testing.JOTTester;
 
 /**
  * Test type resolution
  * @author thibautc
  */
 public class FantomTypesTest extends FantomCSLTest
 {
 
 	@Override
 	public void cslTest() throws Throwable
 	{
		// Testing type sign stuff
		/*FanResolvedType t = FanResolvedType.makeFromTypeSig(null, "Str");
		JOTTester.checkIf("Type sig 1", t.getQualifiedType().equals("Str") && ! t.isNullable());
		t = FanResolvedType.makeFromTypeSig(null, "Str?");
		JOTTester.checkIf("Type sig 2", t.getQualifiedType().equals("Str") && t.isNullable());
		t = FanResolvedType.makeFromTypeSig(null, "Str[]");
		JOTTester.checkIf("Type sig 3", (t instanceof FanResolvedListType) && t.toTypeSig(true).equals("sys::Str[]"));*/
		
 		// Testing isCompatible()
 		JOTTester.checkIf("Compatibility of Enum vs Obj", mkt("sys::Enum").isTypeCompatible(mkt("sys::Obj")));
 		JOTTester.checkIf("Compatibility of Obj vs Obj", mkt("sys::Obj").isTypeCompatible(mkt("sys::Obj")));
 		JOTTester.checkIf("Compatibility of mixin vs Obj", ! mkt("web::Weblet").isTypeCompatible(mkt("sys::Obj")));
 		JOTTester.checkIf("Compatibility of Button vs Obj", mkt("fwt::Button").isTypeCompatible(mkt("sys::Obj")));
 		JOTTester.checkIf("Compatibility of Button vs widget", mkt("fwt::Button").isTypeCompatible( mkt("fwt::Widget")));
 		// Testing getParent()
 		FanResolvedType t = mkt("sys::Actor");
 		FanResolvedType p = t.getParentType();
 		JOTTester.checkIf("Actor parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("sys::Weekday");
 		p = t.getParentType();
 		JOTTester.checkIf("Weekday parent is Enum", p.getDbType().getQualifiedName().equals("sys::Enum"), p.toString());
 		t = mkt("sys::Enum");
 		p = t.getParentType();
 		JOTTester.checkIf("Enum parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("web::Weblet");
 		p = t.getParentType();
 		JOTTester.checkIf("Mixin parent is null", p==null, p==null?"null":p.toString());
 	}
 
 	private FanResolvedType mkt(String qType)
 	{
 		return FanResolvedType.makeFromDbType(null, qType);
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomTypesTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 }
