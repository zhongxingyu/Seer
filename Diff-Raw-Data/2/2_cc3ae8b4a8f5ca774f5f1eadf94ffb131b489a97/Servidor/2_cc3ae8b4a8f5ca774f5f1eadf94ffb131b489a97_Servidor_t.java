 package ep2;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.xml.ws.Endpoint;
 
 import ep2.base.server.Fabrica;
 
 public class Servidor {
 	private static ListaDePedidos listaDePedidos;
 	private static CasaDeMaquinas casaDeMaquinas;
 	private static EspecificacoesDeMaquinas especificacoes;
 	private static int numeroDeCores;
 	private static LinkedList<OfficeBoy> officeBoys;
 
 	public static void main(String[] args) throws Exception {
 		numeroDeCores = Runtime.getRuntime().availableProcessors();
 //		numeroDeCores = 1;
 
 		ParserDeArquivo parser = new ParserDeArquivo(args[0]);
 		parser.executa();
 
 		System.out.println("Fim do Parse");
 
 		especificacoes = parser.getEspecificacoes();
 		casaDeMaquinas = new CasaDeMaquinas(especificacoes);
 
 		listaDePedidos = new ListaDePedidos();
 		criaEInicializaOfficeBoys();
 
 		System.out.println("Fim da inicializacao dos office boys");
 
 		WS();
 
 		System.out.println("Fim do WS");
 
 		mataOfficeBoys();
 		System.out.println("Matou todos os office boys");
 
 		casaDeMaquinas.fecha();
 		System.out.println("Fechou Casa de Maquinas");
 	}
 
 	private static void criaEInicializaOfficeBoys() {
 		officeBoys = new LinkedList<OfficeBoy>();
 		for (int i = 0; i < numeroDeCores; i++) {
 			OfficeBoy officeBoy = new OfficeBoy(especificacoes, listaDePedidos, casaDeMaquinas);
 			officeBoys.add(officeBoy);
 			officeBoy.start();
 		}
 	}
 
 	private static void mataOfficeBoys() {
 		for (OfficeBoy officeBoy : officeBoys)
 			officeBoy.interrupt();
 	}
 
 	private static void WS() throws IOException {
 		ExecutorService threads = Executors.newCachedThreadPool();
 		Fabrica service = new Fabrica();
 		final Endpoint endpoint = Endpoint.create(service);
 		endpoint.setExecutor(threads);
 		Thread th = new Thread() {
 			public void run() {
				endpoint.publish("http://localhost:8000/fabrica");
 			}
 		};
 		th.start();
 		System.out.println("Digite algo seguido de enter para finalizar");
 		System.in.read();
 		endpoint.stop();
 		threads.shutdown();
 	}
 
 	public static boolean getStatusDoPedido(long id) {
 		return listaDePedidos.acessaPedido(id).finalizado();
 	}
 
 	public static long criaPedido(int[] produtos, int[] quantidades) {
 		return listaDePedidos.adicionaPedido(new Pedido(produtos, quantidades));
 	}
 
 }
