 package Controller;
 
 import gui.ConstantesUI;
 import gui.PainelFaixas;
 import gui.PainelTagsGerais;
 import gui.PopUp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 import org.jaudiotagger.tag.datatype.Artwork;
 
 import util.PropertiesFile;
 import Base.Tags;
 import Base.TipoPopUp;
 import Exception.ImagemVaziaException;
 import Exception.PastaDeMusicaNaoExisteException;
 import Exception.PastaDeMusicaVaziaException;
 
 public class ControllerBotoes {
 
 	private Controller controller;
 
 	public ControllerBotoes() {
 		controller = Controller.getInstace();
 
 	}
 
 	/**
 	 * Método chamado pelo botão "salvar". Faz basicamente 3 coisas: 1 - Seta as
 	 * tags 2 - Cria a arvore de diretorio de acordo com a necessidade 3 - move
 	 * os .mp3 corrigidos para a arvore
 	 */
 	public void salvar() {
 		
 		String diretorioDeMusica = PropertiesFile.getProperties();
 		try {
 			PropertiesFile.verifyMusicFolder(diretorioDeMusica);
 		} catch (PastaDeMusicaNaoExisteException e1) {
 			new PopUp(ConstantesUI.POPUP_DIRETORIO_DE_MUSICA_INVALIDO, TipoPopUp.ERROR);
 			return;
 		} catch (PastaDeMusicaVaziaException e1) {
 			new PopUp(ConstantesUI.POPUP_INFORME_DIRETORIO_DE_MUSICA, TipoPopUp.WARNING);
 			return;
 		}
 		
 		ArrayList<Tags> listaTags = controller.getListaTags();
 		PainelFaixas painelFaixas = controller.getPainelFaixas();
 		PainelTagsGerais painelTagsGerais = controller.getPainelTagsGerais();
 		ControllerImage controllerImage = ControllerImage.getInstace();
 
 		// Verifica se algum disco já foi carregado
 		if (listaTags == null) {
 			new PopUp(ConstantesUI.POPUP_CARREGUE_UM_DISCO, TipoPopUp.WARNING);
 			return;
 		}
 
 		// criando um Array de File e alimentando ele com o path de todas as
 		// músicas, já ordenadas
 		List<File> musicas = new ArrayList<File>();
 		for (int i = 0; i < painelFaixas.getTextFieldLabels().size(); i++) {
 			String faixaPath = painelFaixas.getTextFieldLabels().get(i)
 					.getText();
 			String discoPath = controller.getDisco().getAbsolutePath();
 			String path = discoPath + File.separator + faixaPath;
 			musicas.add(new File(path));
 		}
 
 		// Maneira meio louca de garantir a informação dentro do listaTags. E
 		// ainda ordenado
 		for (int i = 0; i < musicas.size(); i++) {
 			listaTags.get(i).setArtista(
 					painelTagsGerais.getTextFieldArtista().getText());
 			listaTags.get(i).setAlbum(
 					painelTagsGerais.getTextFieldAlbum().getText());
 			listaTags.get(i).setAno(
 					painelTagsGerais.getTextFieldAno().getText());
 			listaTags.get(i).setGenero(
 					painelTagsGerais.getTextFieldGenero().getText());
 			listaTags.get(i).setNumero(
 					painelFaixas.getTextFieldsNumero().get(i).getText());
 			listaTags.get(i).setNomeDaMusica(
 					painelFaixas.getTextFieldsFaixas().get(i).getText());
 			listaTags.get(i).setImage(controllerImage.getImagem());
 		}
 
 		// Seta todas as tags
 		try {
 			setTags(listaTags, musicas);
 		} catch (NullPointerException e) {
 			new PopUp(ConstantesUI.POPUP_CAMPOS_OBRIGATORIOS, TipoPopUp.WARNING);
 		} catch (ReadOnlyFileException e) {
 			new PopUp(ConstantesUI.POPUP_ARQUIVO_APENAS_DE_LEITURA,
 					TipoPopUp.ERROR);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Renomea os aquivos
 		renameFile(listaTags, musicas);
 
 		// Criação do diretorio
 		File tempDir = tempDirCreate(diretorioDeMusica, listaTags);
 		if (tempDir != null) {
 			if (!tempDir.mkdirs()) {
 				new PopUp(ConstantesUI.POPUP_FALHA_CRIACAO_DIRETORIO,
 						TipoPopUp.ERROR);
 				return;
 			}
 		}
 
 		// Movendo os arquivos para a pasta de musica
 		if (tempDir != null) {
 			moveFile(musicas, tempDir);
 			File diretorioDeOrigem = musicas.get(0).getParentFile();
 			System.out.println("deletando: " + diretorioDeOrigem);
 			delete(diretorioDeOrigem);
 		}
 
 		new PopUp(ConstantesUI.POPUP_SALVO_COM_SUCESSO, TipoPopUp.INFO);
 
 	}
 
 	/**
 	 * Método auxiliar, para deletar uma pasta recursivamente
 	 * 
 	 * @param Arquivo a ser deletado
 	 */
 	private void delete(File file) {
 		if (file.isDirectory()) {
 			// diretório ta vazio, entao, delete ele
 			if (file.list().length == 0) {
 				file.delete();
 				System.out.println("Diretorio foi deletado: "
 						+ file.getAbsolutePath());
 			} else {
 				// lista todos os arquivos do diretorio
 				String[] files = file.list();
 
 				for (String temp : files) {
 					// contrói a estrutura de arquivo
 					File fileDelete = new File(file, temp);
 					// deleta recursivo
 					delete(fileDelete);
 				}
 
 				// checa o diretorio de novo, se tiver vazio, deleta
 				if (file.list().length == 0) {
 					file.delete();
 					System.out.println("Diretorio foi deletado: "
 							+ file.getAbsolutePath());
 				}
 			}
 		} else {
 			// se for arquivo, entao, delete ele
 			file.delete();
 			System.out.println("Arquivo foi deletado: "
 					+ file.getAbsolutePath());
 		}
 	}
 
 	/**
 	 * Método auxiliar para mover uma lsia de arquivo para um diretório
 	 * 
 	 * @param musicas
 	 * @param tempDir
 	 */
 	private void moveFile(List<File> musicas, File tempDir) {
 		InputStream inStream = null;
 		OutputStream outStream = null;
 		for (int i = 0; i < musicas.size(); i++) {
 			try {
 				File origem = new File(musicas.get(i).getPath());
 				String nomeDaMusica = musicas.get(i).getName();
 				File destino = new File(tempDir.getPath() + File.separator
 						+ nomeDaMusica);
 
 				System.out.println("Origem = " + origem.getPath());
 				System.out.println("Destino = " + destino.getPath());
 
 				inStream = new FileInputStream(origem);
 				outStream = new FileOutputStream(destino);
 
 				byte[] buffer = new byte[1024];
 				int len = 0;
 
 				// Copiando os bytes do arquivos
 				while ((len = inStream.read(buffer)) > 0) {
 					outStream.write(buffer, 0, len);
 				}
 
 				inStream.close();
 				outStream.close();
 
 				// delete the original file
 				origem.delete();
 				System.out.println("File is copied successful!");
 
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Método auxiliar, para criar a arvore de diretorio de acordo com a
 	 * necessidade.
 	 * 
 	 * @param diretorioDeMusica
 	 * @param listaTags
 	 * @return Objeto File criado
 	 */
 	private File tempDirCreate(String diretorioDeMusica,
 			ArrayList<Tags> listaTags) {
 		File tempDir = null;
 		String artista = listaTags.get(0).getArtista();
 		String album = listaTags.get(0).getAno() + " - "
 				+ listaTags.get(0).getAlbum();
 		// Verifica se existe um artista na psta de musica com esse nome, se
 		// existi, entra na pasta
 		if (search(diretorioDeMusica, artista)) {
 			String diretorioDoArtista = diretorioDeMusica + File.separator
 					+ artista;
 			/**
 			 * O if está negado, pq se eu achar um album com o mesmo nome, eh pq
 			 * a minha pasta de origem é exatamente essa, entao, nao precisa
 			 * fazer nada ,basta setar as tags e pronto
 			 */
 			if (!search(diretorioDoArtista, album)) {
 				System.out.println("Existe artista, mas nao existe o album");
 				tempDir = new File(diretorioDoArtista + File.separator + album);
 				System.out.println("tempDir = " + tempDir.getPath());
 			}
 		} else {
 			System.out.println("nao tem artista com esse nome");
 			tempDir = new File(diretorioDeMusica + File.separator + artista
 					+ File.separator + album);
 			System.out.println("tempDir = " + tempDir.getPath());
 		}
 		return tempDir;
 	}
 
 	/**
 	 * Método auxiliar, para renomar uma lista de arquivos, baseados em uma
 	 * lista de tags
 	 * 
 	 * @param listaTags
 	 * @param musicas
 	 */
 	private void renameFile(ArrayList<Tags> listaTags, List<File> musicas) {
 		for (int i = 0; i < musicas.size(); i++) {
 			String path = musicas.get(i).getParent();
 			String nomeCorreto = path + File.separator
 					+ listaTags.get(i).getNumero() + " - "
 					+ listaTags.get(i).getNomeDaMusica() + ".mp3";
 			File nomeCorretoFile = new File(nomeCorreto);
 			musicas.get(i).renameTo(nomeCorretoFile);
 		}
 	}
 
 	/**
 	 * Método auxiliar, para procurar um disco ou um album, no diretorio de
 	 * musica
 	 * 
 	 * @param diretorioDeMusica
 	 * @param name
 	 * @return true se encontrou a busca, e false se nao encontrou
 	 */
 	private boolean search(String diretorioDeMusica, String name) {
 		boolean result = false;
 		File diretorioDeMusicaFile = new File(diretorioDeMusica);
 
 		if (!diretorioDeMusicaFile.isDirectory()) {
 			System.err.println("Nao eh um diretorio");
 			return result;
 		}
 
 		if (diretorioDeMusicaFile.listFiles().length <= 0) {
 			System.err.println("Diretorio vazio");
 			return result;
 		}
 
 		for (int i = 0; i < diretorioDeMusicaFile.listFiles().length; i++) {
 			String busca = diretorioDeMusicaFile.listFiles()[i].getName();
 			// Verifica se existe, se existir, entra na pasta
 			if (name.equals(busca)) {
 				result = true;
 				return result;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Método auxiliar, para setar as tags de uma lista de .mp3
 	 * 
 	 * @param listaTags
 	 * @param musicas
 	 * @throws ReadOnlyFileException
 	 * @throws NullPointerException
 	 * @throws Exception
 	 */
 	private void setTags(ArrayList<Tags> listaTags, List<File> musicas)
 			throws ReadOnlyFileException, NullPointerException, Exception {
 		int erro = 0;
 		for (int i = 0; i < musicas.size(); i++) {
 			AudioFile f = AudioFileIO.read(musicas.get(i));
 			Tag tag = f.getTag();
 			tag.setField(FieldKey.ARTIST, listaTags.get(i).getArtista());
 			tag.setField(FieldKey.ALBUM, listaTags.get(i).getAlbum());
 			tag.setField(FieldKey.YEAR, listaTags.get(i).getAno());
 			tag.setField(FieldKey.GENRE, listaTags.get(i).getGenero());
 			tag.setField(FieldKey.TRACK, listaTags.get(i).getNumero());
 			tag.setField(FieldKey.TITLE, listaTags.get(i).getNomeDaMusica());
 			try {
 				Artwork art = setArtwork(listaTags.get(i).getImage());
 				tag.deleteArtworkField();
 				tag.setField(art);
 			} catch (ImagemVaziaException e) {
 				erro++; // Isso me garante, que o pop-up só vai ser exibido 1x
 				if (erro <= 1) {
 					new PopUp(ConstantesUI.POPUP_IMAGE_VAZIA, TipoPopUp.WARNING);
 				}
 			}
 			f.commit();
 		}
 	}
 
 	/**
 	 * Método para setar a imagem na tag
 	 * 
 	 * @param image
 	 * @return
 	 * @throws ImagemVaziaException
 	 */
 	private Artwork setArtwork(byte[] image) throws ImagemVaziaException {
 		if (image != null) {
 			File cover = new File("cover.jpg");
 			FileOutputStream fos = null;
 			Artwork art = null;
 			try {
 				fos = new FileOutputStream(cover);
 				fos.write(image);
 				art = new Artwork();
 				art.setFromFile(cover);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					cover.delete();
 					fos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return art;
 		} else {
 			throw new ImagemVaziaException();
 		}
 	}
 
 	/**
 	 * Escreve os nomes dos arquivos, nos JTextfield do PainelFaixas
 	 */
 	public void nome2Tags() {
 		PainelFaixas painelFaixas = controller.getPainelFaixas();
 		ArrayList<JLabel> nomes = painelFaixas.getTextFieldLabels();
 
 		for (int i = 0; i < nomes.size(); i++) {
 			String faixa = nomes.get(i).getText();
 			painelFaixas.getTextFieldsFaixas().get(i).setText(faixa);
 		}
 	}
 
 }
