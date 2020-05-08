 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controle;
 
 import Log.Log;
 import Modelo.Item;
 import Modelo.Lista;
 import Modelo.Subitem;
 import Persistencia.GravadorXML;
 import Visao.ProjetoFinal;
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.ResourceBundle;
 import javafx.beans.property.BooleanProperty;
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.chart.PieChart;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.control.cell.CheckBoxListCell;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Paint;
 import javafx.stage.FileChooser;
 import javafx.util.Callback;
 import javax.imageio.ImageIO;
 import org.apache.commons.collections.comparators.ComparatorChain;
 
 /**
  *
  * @author Rayan
  */
 public class PrincipalController implements Initializable {
 
     private ProjetoFinal aplicacao = ProjetoFinal.getInstance();
     private Log log = new Log(PrincipalController.class);
     /* TABS */
     @FXML
     private Tab tabMenuGraficos;
     @FXML
     private Tab tabMenuTarefas;
     /* /TABS */
     /* Tab gráficos */
     @FXML
     private PieChart graficoTarefas;
     @FXML
     private ComboBox comboFormato;
     /* /Tab gráficos */
     /* Tab Tarefas */
     /* Lista */
     @FXML
     private Label lblSaudacao;
     @FXML
     private Label lblAlterarDados;
     @FXML
     private Label lblSair;
     @FXML
     private Button adicionarNovaLista;
     @FXML
     private Button removerLista;
     @FXML
     private Button editarLista;
     @FXML
     private ListView listaListas;
     @FXML
     private ComboBox comboOrdenarLista;
     /* /Lista */
     /* Item */
     @FXML
     private Button adicionarItem;
     @FXML
     private Button removerItem;
     @FXML
     private Button editarItem;
     @FXML
     private ComboBox comboOrdenarItem;
     @FXML
     private ListView listaItens;
     /* /Item */
     /* SubItem */
     /* Informacoes do Item */
     @FXML
     private TextField txtNome;
     @FXML
     private TextField txtPrioridade;
     @FXML
     private TextField txtLocal;
     @FXML
     private TextField txtDataCriacao;
     @FXML
     private TextField txtDataFinalizar;
     @FXML
     private TextField txtDataFinalizado;
     /* /Informacoes do Item */
     /* Informacoes do Item */
     @FXML
     private TitledPane accListarSubItens;
     /* Informacoes do Item */
     @FXML
     private TextField txtNomeSubItem;
     @FXML
     private Label lblSubItemInfo;
     @FXML
     private Button btnRemoverSubItem;
     @FXML
     private ListView listaSubItens;
     /* /SubItem */
     /* /Tab Tarefas */
 
     /* tabGraficos */
     @FXML
     private void abrirAbaGraficos() {
         if (tabMenuGraficos.isSelected()) {
             int contRealizadasSemAtraso = 0, contRealizadasComAtraso = 0, contNaoRealizadas = 0;
             for (Lista l : aplicacao.retornarUsuario().getListas()) {
                 for (Item i : l.getListaItens()) {
                     DateFormat d = new SimpleDateFormat("dd/MM/yyyy");
                     try {
                         if (i.getDataFinalizado() != null && !i.getDataFinalizado().isEmpty() && !i.getDataFinalizado().equals(" ")) {
                             if (d.parse(i.getDataFinalizado()).getTime() > d.parse(i.getDataFinalizar()).getTime()) {
                                 contRealizadasComAtraso++;
                             } else if (d.parse(i.getDataFinalizado()).getTime() < d.parse(i.getDataFinalizar()).getTime()) {
                                 contRealizadasSemAtraso++;
                             }
                         } else {
                             contNaoRealizadas++;
                         }
                     } catch (ParseException ex) {
                         log.error("abrirAbaGraficos", "Erro ao realizar conversão de datas", ex);
                     }
 
                 }
             }
             ObservableList<PieChart.Data> pieChartData = null;
             graficoTarefas.setTitle("");
             graficoTarefas.setData(pieChartData);
             pieChartData = FXCollections.observableArrayList(
                     new PieChart.Data("Realizadas sem atraso (" + contRealizadasSemAtraso + ")", contRealizadasSemAtraso),
                     new PieChart.Data("Realizadas com atraso (" + contRealizadasComAtraso + ")", contRealizadasComAtraso),
                     new PieChart.Data("Não realizadas (" + contNaoRealizadas + ")", contNaoRealizadas));
             graficoTarefas.setTitle("Número de tarefas");
             graficoTarefas.setData(pieChartData);
         }
     }
 
     @FXML
     private void salvarGrafico() {
         BufferedImage img = null;
         try {
             img = new Robot().createScreenCapture(
                     new java.awt.Rectangle(
                     (int) aplicacao.stage.getX() + 40, (int) aplicacao.stage.getY() + 85,
                     (int) aplicacao.stage.getWidth() - 65, (int) aplicacao.stage.getHeight() - 130));
         } catch (AWTException ex) {
             log.error("salvarGrafico", "Erro ao capturar imagem da tela.", ex);
         }
         FileChooser fileChooser = new FileChooser();
         FileChooser.ExtensionFilter exts = new FileChooser.ExtensionFilter("Imagens (png, jpg, bmp)", new String[]{"*.png", "*.jpg", "*.bmp"});
         fileChooser.getExtensionFilters().add(exts);
         File caminho = null;
         caminho = fileChooser.showSaveDialog(aplicacao.stage);
         if (caminho != null) {
             String arquivo = caminho.getAbsolutePath();
             if (!arquivo.isEmpty()) {
                 try {
                     ImageIO.write(img, comboFormato.getValue().toString(), new File(arquivo + "." + comboFormato.getValue().toString()));
                 } catch (IOException ex) {
                     log.fatal("salvarGrafico", "Erro fatal ao exportar imagem para arquivo ->" + arquivo, ex);
                 }
 
             }
         }
     }
     /* /tabGraficos */
     /* tabTarefas */
 
     @FXML
     private void abrirAbaTarefas() {
         if (tabMenuTarefas.isSelected()) {
             abrirListas();
         }
     }
 
     @FXML
     public void alterarDados() {
         aplicacao.goTo("AlterarDados");
     }
 
     @FXML
     public void alterarDados_mouseEmCima() {
         lblAlterarDados.setTextFill(Paint.valueOf("darkgray"));
     }
 
     @FXML
     public void alterarDados_mouseFora() {
         lblAlterarDados.setTextFill(Paint.valueOf("gray"));
     }
 
     @FXML
     public void sair() {
         aplicacao.sair();
         aplicacao.goTo("Login");
     }
 
     @FXML
     public void sair_mouseEmCima() {
         lblSair.setTextFill(Paint.valueOf("darkgray"));
     }
 
     @FXML
     public void sair_mouseFora() {
         lblSair.setTextFill(Paint.valueOf("gray"));
     }
     /* lista */
 
     @FXML
     private void abrirListas() {
         ObservableList<String> itens = null;
         listaListas.setItems(itens);
         itens = FXCollections.observableArrayList();
         for (Lista l : aplicacao.retornarUsuario().getListas()) {
             itens.add(l.getNome());
         }
         listaListas.setItems(itens);
     }
 
     @FXML
     private void ordenacaoListaAcao() {
         ComparatorChain chain;
         ArrayList<String> lista = new ArrayList<>();
         ObservableList<String> itens = null;
 
         listaListas.setItems(itens);
         for (Lista l : aplicacao.retornarUsuario().getListas()) {
             lista.add(l.getNome());
         }
         if (comboOrdenarLista.getSelectionModel().getSelectedItem() == "Nome") {
             Collections.sort(lista);
         } else if (comboOrdenarLista.getSelectionModel().getSelectedItem() == "Prioridade") {
             chain = new ComparatorChain();
             chain.addComparator(new listaPrioridade());
             Collections.sort(lista, chain);
         }
         itens = FXCollections.observableArrayList(lista);
         listaListas.setItems(itens);
     }
 
     @FXML
     private void cadastrarLista() {
         aplicacao.setListaTemp(null);
         aplicacao.goTo("GerenciarLista");
     }
 
     @FXML
     private void editarLista() {
         if (listaListas.getSelectionModel().getSelectedItem() != null) {
             Lista temp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             aplicacao.setListaTemp(temp);
             aplicacao.goTo("GerenciarLista");
         }
     }
 
     @FXML
     private void deletarLista() {
         if (listaListas.getSelectionModel().getSelectedItem() != null) {
             Lista temp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             aplicacao.retornarUsuario().removerLista(temp);
             abrirListas();
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
         }
     }
 
     @FXML
     private void selecionarLista() {
         if (listaListas.getSelectionModel().getSelectedItem() != null) {
             Lista temp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             aplicacao.setListaTemp(temp);
             ObservableList<String> itens = null;
             listaSubItens.setItems(itens);
             txtNome.setText("");
             txtLocal.setText("");
             txtDataCriacao.setText("");
             txtPrioridade.setText("");
             txtDataFinalizar.setText("");
             txtDataFinalizado.setText("");
             abrirListaItens();
             ordenacaoItemAcao();
         }
     }
     /* /lista */
     /* item */
 
     @FXML
     private void abrirListaItens() {
         if (listaListas.getSelectionModel().getSelectedItem() != null) {
             ObservableList<String> itens = null;
 
             final Callback<String, ObservableValue<Boolean>> getProperty = new Callback<String, ObservableValue<Boolean>>() {
                 @Override
                 public ObservableValue<Boolean> call(String p) {
                     BooleanProperty truue = new SimpleBooleanProperty();
                     truue.setValue(Boolean.TRUE);
                     BooleanProperty faalse = new SimpleBooleanProperty();
                     faalse.setValue(Boolean.FALSE);
                     for (Item i : aplicacao.getListaTemp().getListaItens()) {
                         if (i.getNome().equals(p)) {
                             if (i.isRealizado()) {
                                 return truue;
                             } else {
                                 return faalse;
                             }
 
                         }
                     }
                     return null;
                 }
             };
             listaItens.setItems(itens);
             itens = FXCollections.observableArrayList();
             int index = aplicacao.retornarUsuario().getListas().indexOf(aplicacao.getListaTemp());
             if (index >= 0) {
 
                 for (Item i : aplicacao.retornarUsuario().getListas().get(index).getListaItens()) {
                     itens.add(i.getNome());
                 }
                 listaItens.setItems(itens);
 
                 Callback<ListView<String>, ListCell<String>> forListView = CheckBoxListCell.forListView(getProperty);
                 listaItens.setCellFactory(forListView);
             }
         }
     }
 
     @FXML
     public void funcaoListItem(MouseEvent e) {
         if (e.getButton() == MouseButton.PRIMARY) {
             selecionarItem();
         } else if (e.getButton() == MouseButton.SECONDARY) {
             BooleanProperty truue = new SimpleBooleanProperty();
             truue.setValue(Boolean.TRUE);
             BooleanProperty faalse = new SimpleBooleanProperty();
             faalse.setValue(Boolean.FALSE);
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item it = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
 
             if (it.isRealizado()) {
                 it.setRealizado(false);
                 it.setSelected((SimpleBooleanProperty) faalse);
             } else {
                 it.setRealizado(true);
                 it.setSelected((SimpleBooleanProperty) truue);
             }
             GravadorXML gravadorXML = new GravadorXML(aplicacao.retornarUsuario());
             abrirListaItens();
         }
     }
 
     @FXML
     private void selecionarItem() {
         if (aplicacao.getListaTemp() != null && listaItens.getSelectionModel().getSelectedItem() != null) {
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             aplicacao.setListaTemp(lTemp);
             aplicacao.setItemTemp(iTemp);
             Item temp = aplicacao.getItemTemp();
             if (temp != null) {
                 txtNome.setText(temp.getNome());
                 txtLocal.setText(temp.getLocal());
                 txtDataCriacao.setText(temp.getDataCriacao());
                 txtPrioridade.setText(String.valueOf(temp.getPrioridade()));
                 txtDataFinalizar.setText(temp.getDataFinalizar());
                 txtDataFinalizado.setText(temp.getDataFinalizado());
                 abrirListaSubItens();
             }
         }
     }
 
     @FXML
     private void ordenacaoItemAcao() {
         ComparatorChain chain;
         ArrayList<String> lista = new ArrayList<>();
         ObservableList<String> itens = null;
        if (aplicacao.getListaTemp()!= null && aplicacao.getItemTemp() != null) {
             listaItens.setItems(itens);
             for (Item l : aplicacao.getListaTemp().getListaItens()) {
                 lista.add(l.getNome());
             }
             if (comboOrdenarItem.getSelectionModel().getSelectedItem() == "Nome") {
                 Collections.sort(lista);
             } else if (comboOrdenarItem.getSelectionModel().getSelectedItem() == "Prioridade") {
                 chain = new ComparatorChain();
                 chain.addComparator(new itemPrioridade());
                 Collections.sort(lista, chain);
             } else if (comboOrdenarItem.getSelectionModel().getSelectedItem() == "Data criação") {
                 chain = new ComparatorChain();
                 chain.addComparator(new itemDataCriacao());
                 Collections.sort(lista, chain);
             } else if (comboOrdenarItem.getSelectionModel().getSelectedItem() == "Data finalizar") {
                 chain = new ComparatorChain();
                 chain.addComparator(new itemDataFinalizar());
                 Collections.sort(lista, chain);
             }
             itens = FXCollections.observableArrayList(lista);
             listaItens.setItems(itens);
         }
     }
 
     @FXML
     private void cadastrarItem() {
         aplicacao.setItemTemp(null);
         aplicacao.goTo("GerenciarItem");
     }
 
     @FXML
     private void editarItem() {
         if (listaItens.getSelectionModel().getSelectedItem() != null && listaListas.getSelectionModel().getSelectedItem() != null) {
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             aplicacao.setListaTemp(lTemp);
             aplicacao.setItemTemp(iTemp);
             aplicacao.goTo("GerenciarItem");
         }
     }
 
     @FXML
     private void deletarItem() {
         if (listaItens.getSelectionModel().getSelectedItem() != null) {
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             aplicacao.retornarUsuario()
                     .encontrarLista(aplicacao.getListaTemp().getNome())
                     .removerItem(iTemp);
             abrirListaItens();
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
         }
     }
 
     @FXML
     private void finalizarItem() {
         if (listaItens.getSelectionModel().getSelectedItem() != null) {
             BooleanProperty truue = new SimpleBooleanProperty();
             truue.setValue(Boolean.TRUE);
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             iTemp.setRealizado(true);
             iTemp.setSelected((SimpleBooleanProperty) truue);
             Date agora = new Date();
             DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             iTemp.setDataFinalizado(df.format(agora));
             abrirListaItens();
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
         }
     }
 
     /* /item */
     /* subitem */
     @FXML
     private void abrirListaSubItens() {
         ObservableList<String> itens = null;
         Callback<String, ObservableValue<Boolean>> getProperty = new Callback<String, ObservableValue<Boolean>>() {
             @Override
             public ObservableValue<Boolean> call(String p) {
                 BooleanProperty truue = new SimpleBooleanProperty();
                 truue.setValue(Boolean.TRUE);
                 BooleanProperty faalse = new SimpleBooleanProperty();
                 faalse.setValue(Boolean.FALSE);
                 for (Subitem s : aplicacao.getItemTemp().getListaSubItens()) {
                     if (s.getNome().equals(p)) {
                         if (s.isRealizado()) {
                             return truue;
                         } else {
                             return faalse;
                         }
                     }
                 }
                 return null;
             }
         };
         listaSubItens.setItems(itens);
         itens = FXCollections.observableArrayList();
         int indexLista = aplicacao.retornarUsuario().getListas().indexOf(aplicacao.getListaTemp());
         int indexItem = aplicacao.retornarUsuario().getListas().get(indexLista).getListaItens().indexOf(aplicacao.getItemTemp());
 
         if (indexLista >= 0 && indexItem >= 0) {
             for (Subitem s : aplicacao.retornarUsuario().getListas().get(indexLista).getListaItens().get(indexItem).getListaSubItens()) {
                 itens.add(s.getNome());
             }
             listaSubItens.setItems(itens);
 
             Callback<ListView<String>, ListCell<String>> forListView = CheckBoxListCell.forListView(getProperty);
             listaSubItens.setCellFactory(forListView);
         }
     }
 
     @FXML
     private void abrirAdicionarSubItem() {
         txtNomeSubItem.setText("");
         lblSubItemInfo.setText("");
     }
 
     @FXML
     private void adicionarSubItem() {
         if (!txtNomeSubItem.getText().isEmpty() && aplicacao.getListaTemp() != null && aplicacao.getItemTemp() != null) {
             lblSubItemInfo.setText("Subitem adicionado com sucesso.");
             lblSubItemInfo.setTextFill(Paint.valueOf("darkgreen"));
             Subitem subitemtemp = new Subitem();
             subitemtemp.setRealizado(false);
             subitemtemp.setNome(txtNomeSubItem.getText());
             aplicacao.retornarUsuario()
                     .encontrarLista(aplicacao.getListaTemp().getNome())
                     .encontrarItem(aplicacao.getItemTemp().getNome())
                     .adicionarSubitem(subitemtemp);
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
             abrirListaSubItens();
             accListarSubItens.setExpanded(true);
         } else {
             lblSubItemInfo.setText("Erro ao adicionar subitem.");
             lblSubItemInfo.setTextFill(Paint.valueOf("red"));
         }
     }
 
     @FXML
     private void deletarSubItem() {
         if (listaSubItens.getSelectionModel().getSelectedItem() != null) {
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             Subitem sTemp = iTemp.encontrarSubitem((String) listaSubItens.getSelectionModel().getSelectedItem());
             aplicacao.retornarUsuario()
                     .encontrarLista(aplicacao.getListaTemp().getNome())
                     .encontrarItem(aplicacao.getItemTemp().getNome())
                     .removerSubItem(sTemp);
             abrirListaSubItens();
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
         }
     }
 
     @FXML
     private void finalizarSubItem(MouseEvent e) {
         if (e.getButton() == MouseButton.PRIMARY && listaSubItens.getSelectionModel().getSelectedItem() != null) {
             Lista lTemp = aplicacao.retornarUsuario().encontrarLista((String) listaListas.getSelectionModel().getSelectedItem());
             Item iTemp = lTemp.encontrarItem((String) listaItens.getSelectionModel().getSelectedItem());
             Subitem sTemp = iTemp.encontrarSubitem((String) listaSubItens.getSelectionModel().getSelectedItem());
             BooleanProperty truue = new SimpleBooleanProperty();
             truue.setValue(Boolean.TRUE);
             sTemp.setSelected((SimpleBooleanProperty) truue);
             sTemp.setRealizado(true);
             abrirListaSubItens();
             GravadorXML gravador = new GravadorXML(aplicacao.retornarUsuario());
         }
     }
     /* /subitem */
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         ObservableList<String> itens = null;
         comboFormato.setItems(itens);
         itens = FXCollections.observableArrayList(
                 "png",
                 "jpg",
                 "bmp");
         comboFormato.setItems(itens);
         comboFormato.setValue(itens.get(0));
         abrirListas();
         itens = null;
         comboOrdenarLista.setItems(itens);
         comboOrdenarItem.setItems(itens);
         itens = FXCollections.observableArrayList(
                 "Nome",
                 "Prioridade");
         comboOrdenarLista.setItems(itens);
         comboOrdenarLista.setValue(itens.get(0));
         itens = FXCollections.observableArrayList(
                 "Nome",
                 "Data criação",
                 "Data finalizar",
                 "Prioridade");
         comboOrdenarItem.setItems(itens);
         comboOrdenarItem.setValue(itens.get(0));
 
         Image imagemAdicionar = new Image(getClass().getResourceAsStream("Imagens/adicionar.png"));
         Image imagemEditar = new Image(getClass().getResourceAsStream("Imagens/editar.png"));
         Image imagemRemover = new Image(getClass().getResourceAsStream("Imagens/remover.png"));
 
         adicionarNovaLista.setGraphic(new ImageView(imagemAdicionar));
         editarLista.setGraphic(new ImageView(imagemEditar));
         removerLista.setGraphic(new ImageView(imagemRemover));
 
         adicionarItem.setGraphic(new ImageView(imagemAdicionar));
         editarItem.setGraphic(new ImageView(imagemEditar));
         removerItem.setGraphic(new ImageView(imagemRemover));
 
         btnRemoverSubItem.setGraphic(new ImageView(imagemRemover));
 
         lblSaudacao.setText("Olá, " + aplicacao.retornarUsuario().getNome());
         lblSaudacao.setTextFill(Paint.valueOf("gray"));
     }
     /* /tabTarefas */
     /* classes para comparador*/
 
     class itemNome implements Comparator {
 
         @Override
         public int compare(Object o1, Object o2) {
             String z1 = (String) o1;
             String z2 = (String) o2;
             return z2.toUpperCase().compareTo(z1.toUpperCase());
         }
     }
 
     class itemPrioridade implements Comparator {
 
         @Override
         public int compare(Object o1, Object o2) {
             Item z1 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o1);
             Item z2 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o2);
             return z2.getPrioridade() - z1.getPrioridade();
         }
     }
 
     class itemDataCriacao implements Comparator {
 
         @Override
         public int compare(Object o1, Object o2) {
             Item z1 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o1);
             Item z2 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o2);
             DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             try {
                 Date dtCriacaoO1 = df.parse(z1.getDataCriacao());
                 Date dtCriacaoO2 = df.parse(z2.getDataCriacao());
                 if (dtCriacaoO2.after(dtCriacaoO1)) {
                     return 1;
                 } else if (dtCriacaoO2.before(dtCriacaoO1)) {
                     return -1;
                 } else {
                     return 0;
                 }
             } catch (ParseException ex) {
                 log.error("itemDataCriacao/compare", "Erro ao realizar parse de datas.", ex);
             }
             return 0;
         }
     }
 
     class itemDataFinalizar implements Comparator {
 
         @Override
         public int compare(Object o1, Object o2) {
             Item z1 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o1);
             Item z2 = aplicacao.retornarUsuario().getListas().get(listaListas.getSelectionModel().getSelectedIndex()).encontrarItem((String) o2);
             DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             try {
                 Date dtCriacaoO1 = df.parse(z1.getDataFinalizar());
                 Date dtCriacaoO2 = df.parse(z2.getDataFinalizar());
                 if (dtCriacaoO2.after(dtCriacaoO1)) {
                     return 1;
                 } else if (dtCriacaoO2.before(dtCriacaoO1)) {
                     return -1;
                 } else {
                     return 0;
                 }
             } catch (ParseException ex) {
                 log.error("itemDataCriacao/compare", "Erro ao realizar parse de datas.", ex);
             }
             return 0;
         }
     }
 
     class listaPrioridade implements Comparator {
 
         @Override
         public int compare(Object o1, Object o2) {
             Lista z1 = aplicacao.retornarUsuario().encontrarLista((String) o1);
             Lista z2 = aplicacao.retornarUsuario().encontrarLista((String) o2);
             return z2.getPrioridade() - z1.getPrioridade();
         }
     }
     /* /classes para comparador */
 }
