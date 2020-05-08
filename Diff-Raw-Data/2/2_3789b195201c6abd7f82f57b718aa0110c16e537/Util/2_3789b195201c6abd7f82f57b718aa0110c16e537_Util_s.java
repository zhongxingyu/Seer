 package supermecado;
 
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.JOptionPane;
 
 import supermercado.anotacao.Validacao;
 import supermercado.exception.ValidaException;
 
 public class Util {
 	
 	static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 	
 	public static long getInterval(Date d1, Date d2) {
 		return (d2.getTime() - d1.getTime())/1000/60/60/24;
 	}
 	
 	public static void show(String msg) {
 		JOptionPane.showMessageDialog(null, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	public static void showError(String msg) {
 		JOptionPane.showMessageDialog(null, msg, "Erro", JOptionPane.ERROR_MESSAGE);
 	}	
 
 	public static String getInput(String msg) {
 		return JOptionPane.showInputDialog(msg);
 	}
 	
 	public static String getString(Date d) {
 		return sdf.format(d);
 	}
 	
 	public static Date getDate(String d) {
 		try {
 			return sdf.parse(d);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static void valida(Object obj)throws ValidaException{
 		StringBuffer msg = new StringBuffer();
		Field[] atributos = obj.getClass().getFields();
 		for(Field f : atributos){
 			f.setAccessible(true);
 			Validacao val = f.getAnnotation(Validacao.class);
 			if (val!= null && val.requerido()) {
 				try {
 					if (f.get(obj) == null) {
 						msg.append(f.getName() + ": " + ValidaException.CAMPO_OBRIGATORIO + "\n");
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		
 	}
 	
 	public static void main(String[] args) {
 		//Data para String
 		Date dataHoje = new Date();
 		System.out.println(dataHoje);
 		show(getString(dataHoje));
 		
 		//String para Data
 		String dataOntem = "07/02/2013";
 		show(dataOntem);
 		System.out.println(getDate(dataOntem));
 		
 		System.out.println(getInterval(getDate("08/02/2013"), getDate("10/02/2013")));
 		
 		show(getInput("Digite uma mensagem a ser exibida!"));
 	}
 }
