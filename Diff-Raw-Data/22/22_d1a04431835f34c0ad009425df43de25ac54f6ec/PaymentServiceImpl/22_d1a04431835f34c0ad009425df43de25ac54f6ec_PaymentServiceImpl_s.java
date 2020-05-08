 package edu.ibs.core.operation.logic;
 
 import edu.ibs.common.dto.*;
 import edu.ibs.common.enums.CardBookType;
 import edu.ibs.common.exceptions.IbsServiceException;
 import edu.ibs.common.interfaces.IPaymentService;
 import edu.ibs.core.controller.exception.FreezedException;
 import edu.ibs.core.entity.*;
 import edu.ibs.core.gwt.EntityTransformer;
 import edu.ibs.core.operation.AdminOperations;
 import edu.ibs.core.operation.UserOperations;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: EgoshinME
  * Date: 27.12.12
  * Time: 4:30
  */
 public class PaymentServiceImpl implements IPaymentService {
 
     private UserOperations userLogic;
 	private AdminOperations adminLogic;
 
 	@Override
 	public BankBookDTO createBankBook(String userId) throws IbsServiceException {
 		BankBookDTO dto = null;
 		try {
			//todo Разобраться, откуда брать валюту и пользователя
			UserDTO userDTO = new UserDTO();
			userDTO.setId(Long.parseLong(userId));
			User user = new User(userDTO);
			Currency currency = adminLogic.getCurrencies().get(0);
			Money money = new Money(0, currency);
			BankBook bankBook = adminLogic.create(user, money);
			dto = EntityTransformer.transformBankBook(bankBook);
 		} catch (Throwable t) {
 			throw new IbsServiceException("Не удалось создать банковский счёт.");
 		}
 		return dto;
 	}
 
 	@Override
 	public void pay(CardBookDTO from, long to, MoneyDTO money) throws IbsServiceException {
 
 	}
 
     @Override
     public List<CardBookDTO> getCardBooks(final UserDTO userDto) throws IbsServiceException {
         User user = new User(userDto);
         List<CardBookDTO> list = new LinkedList<CardBookDTO>();
         try {
             for (CardBook cardBook : getUserLogic().getCardBooks(user)) {
                 list.add(EntityTransformer.transformCardBook(cardBook));
             }
         } catch (Throwable t) {
             throw new IbsServiceException("Ошибка при получении списка карт.");
         }
         return list;
     }
 
 	@Override
 	public List<CurrencyDTO> getCurrencies() throws IbsServiceException {
 		try {
 			List<Currency> currencies = adminLogic.getCurrencies();
 			List<CurrencyDTO> result = new ArrayList<CurrencyDTO>();
 			if (currencies != null) {
 				for (Currency currency : currencies) {
 					result.add(EntityTransformer.transformCurrency(currency));
 				}
 			}
 			return result;
 		} catch (Throwable t) {
 			throw new IbsServiceException("Возникла ошибка при получении списка валют.");
 		}
 	}
 
 	@Override
 	public void requestCard(UserDTO userDTO, String bankBookId, CardBookType cardBookType, CurrencyDTO currencyDTO)
 			throws IbsServiceException {
 
         try {
             if (bankBookId != null && bankBookId.length() > 0) {
                 if (CardBookType.DEBIT.equals(cardBookType)) {
                     BankBookDTO bankBookDTO = new BankBookDTO();
                     bankBookDTO.setId(Integer.parseInt(bankBookId));
                     bankBookDTO.setOwner(userDTO);
                     bankBookDTO.setCurrency(currencyDTO);
                     userLogic.requestDebitCard(new User(userDTO), new BankBook(bankBookDTO));
                 } else if (CardBookType.CREDIT.equals(cardBookType)) {
 
                 }
             }
         } catch (Throwable t) {
             throw new IbsServiceException("Ошибка при обработке запроса на карт-счет.");
         }
 	}
 
     @Override
     public List<BankBookDTO> getBankBooks(UserDTO userDTO) throws IbsServiceException {
         List<BankBookDTO> result = new LinkedList<BankBookDTO>();
         if (userDTO != null) {
             User user = new User(userDTO);
             for (BankBook bankBook : userLogic.getBankBooks(user)) {
                 result.add(EntityTransformer.transformBankBook(bankBook));
             }
         }
         return result;
     }
 
 	@Override
 	public List<CardRequestDTO> getCardRequests() throws IbsServiceException {
 		List<CardRequestDTO> result = new LinkedList<CardRequestDTO>();
 		try {
 			for (CardRequest cardRequest : adminLogic.getAllRequests()) {
 				result.add(EntityTransformer.transformCardRequest(cardRequest));
 			}
 		} catch (Throwable t) {
 			throw new IbsServiceException("Ошибка при получении списка заявок на карт-счета.");
 		}
 		return result;
 	}
 
 	@Override
 	public CardBookDTO approveCardRequest(CardRequestDTO dto) throws IbsServiceException {
 		CardBookDTO cardBookDTO = new CardBookDTO();
 		try {
 			CardRequest cardRequest = new CardRequest(dto);
 			cardBookDTO = EntityTransformer.transformCardBook(adminLogic.approve(cardRequest));
 		} catch (Throwable t) {
 			throw new IbsServiceException("Ошибка при создании карт-счёта.");
 		}
 		return cardBookDTO;
 	}
 
 	@Override
 	public Boolean addMoney(BankBookDTO bankBookDTO, Double amount) throws IbsServiceException {
 		if (bankBookDTO != null && bankBookDTO.getId() != 0) {
 			try {
 				BankBook bankBook = new BankBook(bankBookDTO);
 				long part1 = amount.longValue();
 				String part2Str = String.valueOf(amount - part1);
 				int index = part2Str.indexOf('.');
 				if (index == 0) {
 					index = part2Str.indexOf(',');
 				}
 				part2Str = part2Str.substring(index + 1);
 				int part2 = Integer.valueOf(part2Str);
 				Money money = new Money(part1, part2, bankBook.getCurrency());
 				return adminLogic.addMoney(bankBook, money);
 			} catch (FreezedException fe) {
 				throw new IbsServiceException(fe.getLocalizedMessage());
 			} catch (Throwable t) {
 				throw new IbsServiceException("Не удалось пополнить счёт.");
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public BankBookDTO getBankBook(AccountDTO accountDTO, long id) throws IbsServiceException {
 		try {
 			BankBook bankBook = adminLogic.getBankBook(new Account(accountDTO), id);
 			return EntityTransformer.transformBankBook(bankBook);
 		} catch (Throwable t) {
 			throw new IbsServiceException("Ошибка при получении банковского счёта.");
 		}
 	}
 
 	public UserOperations getUserLogic() {
         return userLogic;
     }
 
     public void setUserLogic(final UserOperations userLogic) {
         this.userLogic = userLogic;
     }
 
     public AdminOperations getAdminLogic() {
         return adminLogic;
     }
 
     public void setAdminLogic(final AdminOperations adminLogic) {
         this.adminLogic = adminLogic;
     }
 }
