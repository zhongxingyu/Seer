 package org.wicket_sapporo.workshop01.page.session;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
 import org.junit.Test;
 import org.wicket_sapporo.workshop01.WS01Application;
 import org.wicket_sapporo.workshop01.WS01Session;
 import org.wicket_sapporo.workshop01.service.IAuthService;
 
 /**
  * @author Hiroto Yamakawa
  */
 public class SimpleSignInPageTest {
 	private WicketTester tester;
 	private IAuthService mAuthService;
 
 	@Before
 	public void setUp() {
 		tester = new WicketTester(new WS01Application());
 		// 以下、Mockito関係のクラスををstatic importしているのでメソッドのみ呼び出し
 		// IAuthServiceのモックオブジェクトが作成される
 		mAuthService = mock(IAuthService.class);
 		// certify メソッドにどんな値が来ても、falseを返す（認証失敗の体で）
 		when(mAuthService.certify(anyString(), anyString())).thenReturn(false);
 		// ただし、certify メソッドに"abcd", 1234 が来たときは、trueを返す（認証成功の体で）
 		when(mAuthService.certify("abcd", "1234")).thenReturn(true);
 		// sessionのフィールドのauthService変数にmAuthServiceが代入される
 	}
 
 	@Test
 	public void ページが表示される() {
 		tester.startPage(new SimpleSignInPage(mAuthService));
 		tester.assertRenderedPage(SimpleSignInPage.class);
 	}
 
 	@Test
 	public void 認証に成功するとSessionが変更され認証後の画面に遷移される() {
 		tester.startPage(new SimpleSignInPage(mAuthService));
 		FormTester formTester = tester.newFormTester("form");
 		formTester.setValue("userId", "abcd");
		formTester.setValue("passphrase", "1234");
 		formTester.submit();
 
 		// Sessionオブジェクトを呼び出す
 		WS01Session session = (WS01Session) tester.getSession();
 		assertThat(session.isSigned(), is(true));
 		tester.assertRenderedPage(SignedPage.class);
 	}
 
 	@Test
 	public void 認証に失敗するとサインイン失敗と表示される() {
 		tester.startPage(new SimpleSignInPage(mAuthService));
 		FormTester formTester = tester.newFormTester("form");
 		formTester.setValue("userId", "abcd");
 		formTester.setValue("passphrase", "12345");
 		formTester.submit();
 		tester.assertFeedback("form:feedback", "サインイン失敗.");
 		tester.assertRenderedPage(SimpleSignInPage.class);
 	}
 
 }
