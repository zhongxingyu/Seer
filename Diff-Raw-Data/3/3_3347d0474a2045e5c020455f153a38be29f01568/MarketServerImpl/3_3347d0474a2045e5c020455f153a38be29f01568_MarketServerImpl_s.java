 package eugene.market.esma.impl;
 
 import com.google.common.annotations.VisibleForTesting;
 import eugene.market.esma.MarketServer;
 import eugene.market.esma.Order;
 import eugene.market.esma.OrderBook;
 import eugene.market.esma.enums.ExecType;
 import eugene.market.esma.enums.OrdStatus;
 import eugene.market.ontology.MarketOntology;
 import eugene.market.ontology.field.AvgPx;
 import eugene.market.ontology.field.CumQty;
 import eugene.market.ontology.field.LeavesQty;
 import eugene.market.ontology.field.OrderID;
 import eugene.market.ontology.field.Symbol;
 import eugene.market.ontology.message.ExecutionReport;
 import eugene.market.ontology.message.NewOrderSingle;
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.content.onto.basic.Action;
 import jade.core.Agent;
 import jade.domain.FIPANames.ContentLanguage;
 import jade.lang.acl.ACLMessage;
 
 import javax.validation.constraints.NotNull;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static eugene.market.esma.Orders.newOrder;
 
 /**
  * Default implementation of {@link MarketServer}.
  *
  * @author Jakub D Kozlowski
  * @since 0.2
  */
 public class MarketServerImpl implements MarketServer {
 
     public static final String LANGUAGE = ContentLanguage.FIPA_SL;
 
     private final Agent agent;
 
     private final OrderBook orderBook;
 
     /**
      * Constructs an instance of {@link MarketServerImpl} for this <code>agent</code> that will use this
      * <code>orderBook</code>.
      *
      * @param agent     owner of this {@link MarketServerImpl}.
      * @param orderBook {@link OrderBookImpl} implementation to operate on.
      */
     public MarketServerImpl(@NotNull final Agent agent, @NotNull final OrderBook orderBook) {
         checkNotNull(agent, "agent cannot be null");
         checkNotNull(orderBook, "orderBook cannot be null");
         this.orderBook = orderBook;
         this.agent = agent;
     }
 
     /**
      * {@inheritDoc}
      */
     public void serveNewOrderSingleRequest(@NotNull final NewOrderSingle newOrderSingle,
                                            @NotNull final ACLMessage request) {
         final Order order = newOrder(newOrderSingle, request.getSender());
 
         if (orderBook.insertOrder(order)) {
             acceptOrder(order, request);
         }
         else {
             rejectOrder(order, request);
         }
     }
 
     /**
      * Sends an {@link ExecutionReport} with {@link ExecType#NEW} status.
      *
      * @param order accepted order.
      * @param request original request.
      */
     private void acceptOrder(@NotNull final Order order, @NotNull final ACLMessage request) {
         updateOrder(order, request, ExecType.NEW, OrdStatus.NEW);
     }
 
     /**
      * Sends an {@link ExecutionReport} with {@link ExecType#REJECTED} status.
      *
      * @param order order to reject.
      * @param request original request.
      */
     private void rejectOrder(@NotNull final Order order, @NotNull final ACLMessage request) {
         updateOrder(order, request, ExecType.REJECTED, OrdStatus.REJECTED);
     }
 
     /**
      * Sends an {@link ExecutionReport} with this <code>execType</code>.
      *
      * @param order    order to send the {@link ExecutionReport} about.
      * @param request original request.
      * @param execType status of the {@link ExecutionReport} to send.
      */
     private void updateOrder(@NotNull final Order order, @NotNull ACLMessage request, @NotNull final ExecType execType,
                              @NotNull final OrdStatus ordStatus) {
 
         final ExecutionReport executionReport = new ExecutionReport();
         executionReport.setAvgPx(new AvgPx(order.getAvgExecutedPrice()));
         executionReport.setExecType(execType.getExecType());
         executionReport.setOrdStatus(ordStatus.getOrdStatus());
         executionReport.setLeavesQty(new LeavesQty(order.getOpenQuantity()));
         executionReport.setCumQty(new CumQty(order.getExecutedQuantity()));
         executionReport.setOrderID(new OrderID(order.getClOrdID()));
         executionReport.setSide(order.getSide().getSide());
         executionReport.setSymbol(new Symbol(order.getSymbol()));
 
         try {
             final Action a = new Action(order.getAID(), executionReport);
 
             final ACLMessage aclMessage = request.createReply();
             if (ExecType.CANCELED == execType) {
                 aclMessage.setPerformative(ACLMessage.REFUSE);
             } else {
                 aclMessage.setPerformative(ACLMessage.INFORM);
             }
 
             aclMessage.addReceiver(order.getAID());
             aclMessage.setOntology(MarketOntology.getInstance().getName());
             aclMessage.setLanguage(LANGUAGE);
             agent.getContentManager().fillContent(aclMessage, a);
             agent.send(aclMessage);
         }
         catch (CodecException e) {
             System.out.println(e);
         }
         catch (OntologyException e) {
             System.out.println(e);
         }
     }
 
     /**
      * Gets the orderBook.
      *
      * @return the orderBook.
      */
     @VisibleForTesting
     public OrderBook getOrderBook() {
         return orderBook;
     }
 }
