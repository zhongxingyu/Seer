 package au.com.showcase.application.server;
 
 import static nl.captcha.Captcha.NAME;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import nl.captcha.Captcha;
 import nl.captcha.backgrounds.GradiatedBackgroundProducer;
 import nl.captcha.gimpy.DropShadowGimpyRenderer;
 import nl.captcha.text.producer.DefaultTextProducer;
 
 import org.apache.commons.codec.binary.Base64;
 
 import au.com.showcase.application.shared.account.RegenerateCaptcha;
 import au.com.showcase.application.shared.account.RegenerateCaptchaResult;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.gwtplatform.dispatch.server.ExecutionContext;
 import com.gwtplatform.dispatch.server.actionhandler.ActionHandler;
 import com.gwtplatform.dispatch.shared.ActionException;
 
 public class RegenerateCaptchaActionHandler implements
 		ActionHandler<RegenerateCaptcha, RegenerateCaptchaResult> {
 
 	private Provider<HttpServletRequest> requestProvider;
 
 	private ServletContext servletContext;
 
 	private HttpServletRequest httpServletRequest;
 
 	private HttpSession httpSession = null;
 
 	@Inject
 	public RegenerateCaptchaActionHandler(
 			Provider<HttpServletRequest> requestProvider,
 			ServletContext servletContext, HttpServletRequest httpServletRequest) {
 		super();
 		this.requestProvider = requestProvider;
 		this.servletContext = servletContext;
 		this.httpServletRequest = httpServletRequest;
 		httpSession = httpServletRequest.getSession();
 	}
 
 	@Override
 	public RegenerateCaptchaResult execute(RegenerateCaptcha action,
 			ExecutionContext context) throws ActionException {
		HttpSession httpSession = requestProvider.get().getSession();

		System.out.println(httpSession);
 
 		// System.out.println("" + httpSession.getId());
 
 		// Captcha captcha = new Captcha.Builder(347, 60)
 		// .addText(new DefaultTextProducer(9))
 		// .addBackground(new GradiatedBackgroundProducer())
 		// .gimp(new DropShadowGimpyRenderer()).addNoise().addBorder()
 		// .build();
 		//
 		// System.out.println("Executed");
 		// httpSession.setAttribute(NAME, captcha);
 		// System.out.println("Executed");
 		// // CaptchaServletUtil.writeImage(resp, captcha.getImage());
 		//
 
 		RegenerateCaptchaResult regenerateCaptchaResult = null;
 		try {
			String temp = encodeasBase64(httpSession);
			regenerateCaptchaResult = new RegenerateCaptchaResult(temp);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return regenerateCaptchaResult;
 	}
 
 	public String encodeasBase64(HttpSession httpSession) {
 		Captcha captcha = new Captcha.Builder(347, 60)
 				.addText(new DefaultTextProducer(9))
 				.addBackground(new GradiatedBackgroundProducer())
 				.gimp(new DropShadowGimpyRenderer()).addNoise().addBorder()
 				.build();
 
 		httpSession.setAttribute(NAME, captcha);
 
 		byte[] res = null;
 		try {
 			BufferedImage image = captcha.getImage();
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ImageIO.write(image, "png", baos);
 			res = baos.toByteArray();
 			// String encodedImage = null;// Base64.encode(baos.toByteArray());
 			// System.out
 			// .println("The encoded image byte array is shown below.Please use this in your webpage image tag.\n"
 			// + encodedImage);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Some problem has occurred. Please try again");
 		}
 		StringBuilder imageDataAsbase64String = new StringBuilder();
 		String encodedString = new String(Base64.encodeBase64(res));
 
 		imageDataAsbase64String.append("data:image/jpg;base64,");
 
 		imageDataAsbase64String.append(encodedString);

 		System.out.println("IMAGE GENERATED....");
 
 		return imageDataAsbase64String.toString();
 	}
 
 	@Override
 	public void undo(RegenerateCaptcha action, RegenerateCaptchaResult result,
 			ExecutionContext context) throws ActionException {
 	}
 
 	@Override
 	public Class<RegenerateCaptcha> getActionType() {
 		return RegenerateCaptcha.class;
 	}
 }
