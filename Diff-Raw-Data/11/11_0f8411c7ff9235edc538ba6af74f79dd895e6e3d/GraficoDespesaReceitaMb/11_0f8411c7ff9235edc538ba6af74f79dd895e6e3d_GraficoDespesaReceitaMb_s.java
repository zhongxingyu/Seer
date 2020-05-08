 package br.com.unifacs.view;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.primefaces.model.chart.CartesianChartModel;
 import org.primefaces.model.chart.ChartSeries;
 
 import br.com.unifacs.bo.LancamentoBo;
 import br.com.unifacs.bo.LancamentoBoImpl;
 import br.com.unifacs.model.Lancamento;
 
 @ManagedBean(name="graficoMb")
 @SessionScoped
 public class GraficoDespesaReceitaMb implements Serializable {
 
 	private CartesianChartModel categoryModel;
 	private LancamentoBo bo;
 	
 	public GraficoDespesaReceitaMb() {
 		bo = new LancamentoBoImpl();
		//criarGrafico();
 	}
 	
 	private void criarGrafico(){
 		
 		categoryModel = new CartesianChartModel();
 		
 		ChartSeries receita = new ChartSeries();
 		receita.setLabel("Receitas");
 		ChartSeries despesa = new ChartSeries();
 		despesa.setLabel("Despesas");
 		
 		List<Lancamento> lancamentos = bo.obterTodos();
 		
 		for(Lancamento l:lancamentos){
 			if(l.getDespesa().equals("S")){
 				despesa.set(l.getDataPgto(), l.getValorPgto());
 			} else {
 				receita.set(l.getDataPgto(), l.getValorPgto());
 			}
 		}
 		
 		categoryModel.addSeries(receita);
 		categoryModel.addSeries(despesa);
 			
 	}
 	
 	public CartesianChartModel getCategoryModel() {
 		return categoryModel;
 	}
 	public void setCategoryModel(CartesianChartModel categoryModel) {
 		this.categoryModel = categoryModel;
 	}
 
 }
